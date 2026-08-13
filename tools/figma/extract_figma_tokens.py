#!/usr/bin/env python3
"""Figma 프레임의 스타일 정보를 추출해 JSON 으로 덤프한다 (STEP 1).

목적은 "Figma 시안과 실제 구현이 다른지"를 눈이 아니라 데이터로 비교하는 것.
그래서 사람이 보기 좋은 요약이 아니라 **기계가 diff 할 수 있는 원본**을 남긴다.

의존성 없음 — 표준 라이브러리만 쓴다 (Python 3.9+).

사용법:
    export FIGMA_TOKEN='figd_...'
    python3 tools/figma/extract_figma_tokens.py --node 1234-5678

    # 여러 프레임을 한 번에
    python3 tools/figma/extract_figma_tokens.py --node 1234-5678,9012-3456

    # Figma 에서 Copy link 한 URL 을 그대로 붙여도 된다
    python3 tools/figma/extract_figma_tokens.py --node 'https://figma.com/design/xxx?node-id=1234-5678'

토큰은 환경변수로만 받는다. 인자로 받으면 셸 히스토리와 ps 출력에 남는다.
"""

import argparse
import json
import os
import re
import sys
import urllib.error
import urllib.parse
import urllib.request
from collections import Counter

API_BASE = "https://api.figma.com/v1"

# 시안 파일이 여러 개(작업용/export용)라 기본값을 박지 않는다.
# Figma URL 을 통째로 주면 거기서 읽고, id 만 줄 거면 --file-key / FIGMA_FILE_KEY 로 넘긴다.
DEFAULT_FILE_KEY = os.environ.get("FIGMA_FILE_KEY")

DEFAULT_OUT = "tools/figma/out/figma-tokens-raw.json"

# 자식을 가질 수 있는 컨테이너. 레이아웃 속성은 여기서만 의미가 있다.
LAYOUT_TYPES = {
    "FRAME", "GROUP", "COMPONENT", "COMPONENT_SET", "INSTANCE", "SECTION",
    "RECTANGLE", "ELLIPSE", "VECTOR", "LINE", "STAR", "POLYGON", "BOOLEAN_OPERATION",
}


class FigmaError(RuntimeError):
    pass


def fetch(path, token, params=None):
    """Figma REST GET. 실패는 원인을 알 수 있는 메시지로 바꿔서 올린다."""
    url = "{}/{}".format(API_BASE, path)
    if params:
        url += "?" + urllib.parse.urlencode(params)
    req = urllib.request.Request(url, headers={"X-Figma-Token": token})
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8", errors="replace")[:300]
        if e.code == 403:
            raise FigmaError(
                "403 — 토큰이 없거나 이 파일에 접근 권한이 없다. "
                "Figma 설정 > Personal access tokens 에서 file_content:read 스코프로 재발급.\n"
                "  응답: {}".format(body)
            )
        if e.code == 404:
            raise FigmaError("404 — file key 가 틀렸다: {}\n  응답: {}".format(url, body))
        if e.code == 429:
            raise FigmaError("429 — 레이트 리밋. 잠시 후 재시도.")
        raise FigmaError("HTTP {} — {}\n  응답: {}".format(e.code, url, body))
    except urllib.error.URLError as e:
        raise FigmaError("네트워크 실패: {}".format(e.reason))


def parse_target(raw):
    """`1234-5678`, `1234:5678`, Figma URL 에서 (file_key, node_id) 를 뽑는다.

    두 가지를 여기서 해결한다.

    1. **node id 형식** — Figma URL 은 `node-id=1234-5678`(하이픈)로 주는데 REST API 는
       콜론을 받는다. 안 바꾸면 노드를 못 찾고 조용히 빈 결과가 나온다.
    2. **file key** — 시안 파일이 여러 개(작업용/export용)라 기본값을 박아두면 엉뚱한
       파일을 보게 된다. URL 을 통째로 붙이면 거기서 그대로 읽어 어긋날 일이 없다.

    file_key 는 URL 로 준 경우에만 나오고, 아니면 None(호출부가 기본값을 쓴다).
    """
    raw = raw.strip()
    file_key = None
    if raw.startswith("http"):
        parsed = urllib.parse.urlparse(raw)
        # /design/{key}/{slug} · /file/{key}/{slug} · /proto/{key}/{slug}
        m = re.match(r"^/(?:design|file|proto|board)/([A-Za-z0-9]+)", parsed.path)
        if m:
            file_key = m.group(1)
        query = urllib.parse.parse_qs(parsed.query)
        ids = query.get("node-id") or query.get("node_id")
        if not ids:
            raise FigmaError("URL 에 node-id 가 없다 (프레임 우클릭 > Copy link): {}".format(raw))
        raw = ids[0]
    if not re.fullmatch(r"[0-9]+[-:][0-9]+", raw):
        raise FigmaError("노드 id 형식이 아니다: {!r} (예: 9-29)".format(raw))
    return file_key, raw.replace("-", ":")


def rgba_hex(color, opacity=None):
    """Figma 의 0~1 실수 색상을 #RRGGBB / #RRGGBBAA 로."""
    if not isinstance(color, dict):
        return None
    def ch(v):
        return max(0, min(255, int(round(float(v) * 255))))
    r, g, b = ch(color.get("r", 0)), ch(color.get("g", 0)), ch(color.get("b", 0))
    a = color.get("a", 1)
    if opacity is not None:
        a = a * opacity
    hex_rgb = "#{:02X}{:02X}{:02X}".format(r, g, b)
    if a is None or abs(a - 1.0) < 1e-6:
        return hex_rgb
    return hex_rgb + "{:02X}".format(ch(a))


def extract_fills(node):
    """단색 fill 만 뽑는다. 그라디언트/이미지는 종류만 남긴다."""
    out = []
    for fill in node.get("fills") or []:
        if fill.get("visible") is False:
            continue
        kind = fill.get("type")
        if kind == "SOLID":
            out.append({"type": kind, "hex": rgba_hex(fill.get("color"), fill.get("opacity"))})
        else:
            out.append({"type": kind})
    return out


def extract_text(node):
    """TEXT 노드의 타이포 메트릭.

    lineHeight 는 Figma 가 px / 폰트크기% 두 단위로 준다. 코드(Type.kt)는 sp 절대값이라
    비교하려면 px 쪽이 필요하다 — 원본과 계산값을 둘 다 남긴다.
    """
    style = node.get("style") or {}
    font_size = style.get("fontSize")
    unit = style.get("lineHeightUnit")
    line_height_px = style.get("lineHeightPx")
    if line_height_px is None and unit == "FONT_SIZE_%" and font_size:
        pct = style.get("lineHeightPercentFontSize")
        if pct is not None:
            line_height_px = font_size * pct / 100.0

    text = {
        "fontFamily": style.get("fontFamily"),
        "fontPostScriptName": style.get("fontPostScriptName"),
        "fontWeight": style.get("fontWeight"),
        "fontSize": font_size,
        "lineHeightUnit": unit,
        "lineHeightPx": line_height_px,
        "lineHeightPercentFontSize": style.get("lineHeightPercentFontSize"),
        "letterSpacing": style.get("letterSpacing"),
        "textCase": style.get("textCase"),
        "textAlignHorizontal": style.get("textAlignHorizontal"),
        "textAlignVertical": style.get("textAlignVertical"),
    }
    chars = node.get("characters")
    if chars:
        # 원문 전체는 필요 없고, 어느 요소인지 알아볼 정도만.
        text["sample"] = chars[:40].replace("\n", " ")
    return {k: v for k, v in text.items() if v is not None}


def extract_layout(node):
    keys = (
        "layoutMode", "layoutWrap", "layoutSizingHorizontal", "layoutSizingVertical",
        "primaryAxisAlignItems", "counterAxisAlignItems",
        "primaryAxisSizingMode", "counterAxisSizingMode",
        "itemSpacing", "counterAxisSpacing",
        "paddingLeft", "paddingRight", "paddingTop", "paddingBottom",
        "cornerRadius", "rectangleCornerRadii", "strokeWeight", "strokeAlign",
        "opacity", "clipsContent",
    )
    out = {k: node[k] for k in keys if k in node and node[k] is not None}
    box = node.get("absoluteBoundingBox")
    if isinstance(box, dict):
        out["width"] = box.get("width")
        out["height"] = box.get("height")
    return out


def walk(node, path, acc, counter):
    """노드 트리를 깊이 우선으로 훑으며 스타일을 모은다."""
    node_type = node.get("type")
    name = node.get("name") or "(unnamed)"
    full_path = path + [name]
    counter[node_type] += 1

    entry = {
        "id": node.get("id"),
        "name": name,
        "type": node_type,
        "path": " / ".join(full_path),
        # styleId 는 Figma 의 "스타일" 연결(Heading/H3 등), boundVariables 는 변수 연결.
        # 둘 다 없는 노드가 곧 "시안에서 토큰을 안 쓰고 손으로 값을 넣은 곳"이다.
        "styleIds": node.get("styles") or {},
        "boundVariables": node.get("boundVariables") or {},
    }

    if node_type == "TEXT":
        entry["text"] = extract_text(node)
    if node_type in LAYOUT_TYPES:
        layout = extract_layout(node)
        if layout:
            entry["layout"] = layout
    fills = extract_fills(node)
    if fills:
        entry["fills"] = fills

    acc.append(entry)

    for child in node.get("children") or []:
        walk(child, full_path, acc, counter)


def collect_style_names(nodes_payload, published):
    """styleId → 사람이 읽는 이름.

    두 군데서 온다:
      - 파일 응답 안의 `styles` 맵: 그 파일에 실제로 쓰인 스타일(로컬 포함). 이게 주력.
      - /styles 엔드포인트: 팀 라이브러리로 **게시된** 스타일만. 게시 안 했으면 비어 있다.
    로컬 쪽을 우선한다 — 실제로 이 프레임이 참조하는 이름이라서.
    """
    names = {}
    for style_id, meta in (published or {}).items():
        names[style_id] = {"name": meta.get("name"), "styleType": meta.get("style_type"), "source": "published"}
    for node in (nodes_payload.get("nodes") or {}).values():
        if not node:
            continue
        for style_id, meta in (node.get("styles") or {}).items():
            names[style_id] = {
                "name": meta.get("name"),
                "styleType": meta.get("styleType"),
                "source": "file",
            }
    return names


def fetch_published_styles(file_key, token):
    """게시된 스타일 목록. 실패해도 치명적이지 않으므로 경고만 남기고 넘어간다."""
    try:
        payload = fetch("files/{}/styles".format(file_key), token)
    except FigmaError as e:
        print("  경고: 게시 스타일 조회 실패 — 로컬 스타일만 쓴다. ({})".format(e), file=sys.stderr)
        return {}
    return {s.get("node_id"): s for s in (payload.get("meta") or {}).get("styles", [])}


def main():
    parser = argparse.ArgumentParser(description="Figma 노드 스타일 추출 (STEP 1)")
    parser.add_argument("--node", required=True,
                        help="노드 id 또는 Figma URL. 쉼표로 여러 개.")
    parser.add_argument("--file-key", default=DEFAULT_FILE_KEY,
                        help="URL 대신 노드 id 만 줄 때 필요. URL 을 주면 무시된다.")
    parser.add_argument("--out", default=DEFAULT_OUT)
    args = parser.parse_args()

    token = os.environ.get("FIGMA_TOKEN") or os.environ.get("FIGMA_PERSONAL_ACCESS_TOKEN")
    if not token:
        print(
            "FIGMA_TOKEN 이 없다. 발급 후 환경변수로 넘겨라 (인자로 주면 셸 히스토리에 남는다):\n"
            "    export FIGMA_TOKEN='figd_...'",
            file=sys.stderr,
        )
        return 2

    try:
        targets = [parse_target(n) for n in args.node.split(",") if n.strip()]
    except FigmaError as e:
        print(str(e), file=sys.stderr)
        return 2

    node_ids = [node_id for _, node_id in targets]
    # URL 에서 읽은 file key 가 있으면 그게 우선 — 명시적으로 준 것이라서.
    url_keys = {key for key, _ in targets if key}
    if len(url_keys) > 1:
        print("URL 들의 file key 가 서로 다르다: {}\n같은 파일의 노드만 한 번에 넣어라."
              .format(", ".join(sorted(url_keys))), file=sys.stderr)
        return 2
    file_key = url_keys.pop() if url_keys else args.file_key
    if not file_key:
        print("file key 가 없다. Figma URL 을 통째로 주거나 --file-key 를 넘겨라.", file=sys.stderr)
        return 2

    print("파일 {} / 노드 {}".format(file_key, ", ".join(node_ids)))

    try:
        payload = fetch("files/{}/nodes".format(file_key), token, {"ids": ",".join(node_ids)})
        published = fetch_published_styles(file_key, token)
    except FigmaError as e:
        print(str(e), file=sys.stderr)
        return 1

    style_names = collect_style_names(payload, published)

    frames = {}
    counter = Counter()
    missing_ids = []
    for node_id in node_ids:
        wrapper = (payload.get("nodes") or {}).get(node_id)
        if not wrapper or not wrapper.get("document"):
            missing_ids.append(node_id)
            continue
        doc = wrapper["document"]
        acc = []
        walk(doc, [], acc, counter)
        frames[node_id] = {"name": doc.get("name"), "nodes": acc}

    if missing_ids:
        print("  경고: 응답에 없는 노드 — {}".format(", ".join(missing_ids)), file=sys.stderr)

    result = {
        "fileKey": file_key,
        "requestedNodeIds": node_ids,
        "lastModified": payload.get("lastModified"),
        "version": payload.get("version"),
        "styleNames": style_names,
        "frames": frames,
    }

    out_path = os.path.abspath(args.out)
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=2, sort_keys=True)

    summarize(frames, style_names, out_path, counter)
    return 0


def summarize(frames, style_names, out_path, counter):
    total = sum(len(f["nodes"]) for f in frames.values())
    texts = []
    for frame in frames.values():
        texts.extend(n for n in frame["nodes"] if n["type"] == "TEXT")

    text_no_style = [n for n in texts if not n["styleIds"].get("text") and not n["boundVariables"]]
    filled = [n for n in frame_nodes(frames) if n.get("fills")]
    fill_no_token = [
        n for n in filled
        if not n["styleIds"].get("fill") and "fills" not in n["boundVariables"]
    ]
    spacing_nodes = [
        n for n in frame_nodes(frames)
        if any(k in n.get("layout", {}) for k in
               ("itemSpacing", "paddingLeft", "paddingRight", "paddingTop", "paddingBottom"))
    ]

    print("\n저장: {}".format(out_path))
    print("노드 {}개 (프레임 {}개)".format(total, len(frames)))
    print("  타입별: " + ", ".join("{}={}".format(t, c) for t, c in counter.most_common()))
    print("  스타일 이름 매핑: {}개".format(len(style_names)))
    print("\n토큰이 안 붙은 곳 — STEP 2 에서 REVIEW_NEEDED 로 갈 후보:")
    print("  TEXT {}개 중 텍스트 스타일/변수 없음: {}개".format(len(texts), len(text_no_style)))
    print("  fill 있는 노드 {}개 중 색 스타일/변수 없음: {}개".format(len(filled), len(fill_no_token)))
    print("  간격·패딩 값을 가진 노드: {}개 (Figma 는 spacing 을 변수로 안 묶는 경우가 많다)"
          .format(len(spacing_nodes)))
    for n in text_no_style[:10]:
        size = n.get("text", {}).get("fontSize")
        weight = n.get("text", {}).get("fontWeight")
        print("    - {} (size={}, weight={})".format(n["path"], size, weight))
    if len(text_no_style) > 10:
        print("    ... 외 {}개".format(len(text_no_style) - 10))


def frame_nodes(frames):
    for frame in frames.values():
        for node in frame["nodes"]:
            yield node


if __name__ == "__main__":
    sys.exit(main())
