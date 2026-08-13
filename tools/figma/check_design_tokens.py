#!/usr/bin/env python3
"""디자인 토큰이 시안에서 벗어났는지 검사한다. 어긋나면 exit 1.

왜 필요한가 — 눈으로 안 걸리기 때문이다. `c2` 는 lineHeight 가 19.6 이어야 하는데 아래
12sp 행에서 복사된 16.8 이 들어가 있었고, 24곳에서 2.8sp 씩 어긋난 채 코드리뷰를 통과했다.
"살짝 빽빽하다"는 리뷰로 잡히지 않는다. 그래서 사람이 아니라 CI 가 본다.

검사 두 가지:

1. **스냅샷 대조** — figma-tokens.lock.json 에 박아둔 시안 값과 Color.kt / Type.kt 를 비교.
2. **비율 규칙** — 시안 전체가 `lineHeight = fontSize x 1.4` 다. 스냅샷에 없는 토큰까지
   포함해 **모든** 타이포에 적용한다. 아직 대조 못 한 프레임의 토큰도 이 규칙으로는 걸린다.

Figma 네트워크 호출을 하지 않는다 — CI 에 Figma 토큰을 넣지 않기 위해서다. 시안이 바뀌면
사람이 lock 파일을 갱신해 커밋한다(절차는 README).

사용법:
    python3 tools/figma/check_design_tokens.py
"""

import json
import os
import re
import sys

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
LOCK_PATH = os.path.join(REPO_ROOT, "tools", "figma", "figma-tokens.lock.json")
THEME_DIR = os.path.join(
    REPO_ROOT, "core", "designsystem", "src", "main", "java",
    "com", "makeus", "mody", "core", "designsystem", "theme",
)
COLOR_KT = os.path.join(THEME_DIR, "Color.kt")
TYPE_KT = os.path.join(THEME_DIR, "Type.kt")

# Compose FontWeight → 시안이 쓰는 수치.
WEIGHTS = {"Thin": 100, "ExtraLight": 200, "Light": 300, "Normal": 400,
           "Medium": 500, "SemiBold": 600, "Bold": 700, "ExtraBold": 800, "Black": 900}

# val Primary0 = Color(0xFFFBD406)
COLOR_RE = re.compile(r"^val\s+(\w+)\s*=\s*Color\(0x([0-9A-Fa-f]{8})\)", re.MULTILINE)
# val c2: TextStyle = modyTextStyle(FontWeight.Medium, 14.sp, 19.6.sp)
TYPE_RE = re.compile(
    r"val\s+(\w+)\s*:\s*TextStyle\s*=\s*modyTextStyle\(\s*"
    r"FontWeight\.(\w+)\s*,\s*([\d.]+)\.sp\s*,\s*([\d.]+)\.sp"
)

# 부동소수 비교 여유. sp 값이 소수 첫째 자리까지라 이 정도면 충분하다.
EPSILON = 0.01


def read(path):
    if not os.path.isfile(path):
        raise SystemExit("파일이 없다: {}".format(path))
    with open(path, encoding="utf-8") as f:
        return f.read()


def parse_colors(src):
    """`val Name = Color(0xAARRGGBB)` → {name: "RRGGBB"}. 알파는 따로 돌려준다."""
    out = {}
    for name, argb in COLOR_RE.findall(src):
        out[name] = {"alpha": argb[:2].upper(), "hex": argb[2:].upper()}
    return out


def parse_typography(src):
    out = {}
    for name, weight, size, line_height in TYPE_RE.findall(src):
        out[name] = {
            "weight": WEIGHTS.get(weight),
            "weightName": weight,
            "size": float(size),
            "lineHeight": float(line_height),
        }
    return out


def check_colors(lock, colors, errors):
    for figma_name, spec in lock["colors"].items():
        code_name = spec["code"]
        actual = colors.get(code_name)
        if actual is None:
            errors.append("색 `{}` 이(가) Color.kt 에 없다 (시안 `{}`)".format(code_name, figma_name))
            continue
        if actual["hex"] != spec["hex"].upper():
            errors.append(
                "색 `{}`: 시안 `{}` = #{} 인데 코드는 #{}".format(
                    code_name, figma_name, spec["hex"].upper(), actual["hex"]))
        # 팔레트는 전부 불투명하다. 알파가 끼면 화면에서 색이 흐려진다.
        if actual["alpha"] != "FF":
            errors.append("색 `{}`: 알파가 0x{} 다 (팔레트는 FF 여야 한다)".format(code_name, actual["alpha"]))


def check_typography(lock, typo, errors):
    ratio = lock["lineHeightRatio"]

    for figma_name, spec in lock["typography"].items():
        code_name = spec["code"]
        actual = typo.get(code_name)
        if actual is None:
            errors.append("타이포 `{}` 이(가) Type.kt 에 없다 (시안 `{}`)".format(code_name, figma_name))
            continue
        if abs(actual["size"] - spec["size"]) > EPSILON:
            errors.append("타이포 `{}`: 시안 `{}` fontSize {} 인데 코드는 {}".format(
                code_name, figma_name, spec["size"], actual["size"]))
        if actual["weight"] != spec["weight"]:
            errors.append("타이포 `{}`: 시안 `{}` weight {} 인데 코드는 {}({})".format(
                code_name, figma_name, spec["weight"], actual["weight"], actual["weightName"]))

    # 스냅샷에 없는 토큰까지 포함해 전부 검사한다 — 이 규칙이 c2 를 잡았다.
    for code_name, actual in sorted(typo.items()):
        expected = round(actual["size"] * ratio, 2)
        if abs(actual["lineHeight"] - expected) > EPSILON:
            errors.append(
                "타이포 `{}`: lineHeight 가 {} 인데 규칙상 {} x {} = {} 여야 한다".format(
                    code_name, actual["lineHeight"], actual["size"], ratio, expected))


def main():
    lock = json.loads(read(LOCK_PATH))
    colors = parse_colors(read(COLOR_KT))
    typo = parse_typography(read(TYPE_KT))

    if not colors or not typo:
        print("Color.kt / Type.kt 파싱에 실패했다 — 토큰 선언 형식이 바뀌었는지 확인해라.",
              file=sys.stderr)
        print("  색 {}개, 타이포 {}개 파싱".format(len(colors), len(typo)), file=sys.stderr)
        return 2

    errors = []
    check_colors(lock, colors, errors)
    check_typography(lock, typo, errors)

    locked_colors = len(lock["colors"])
    locked_typo = len(lock["typography"])
    print("색 {}/{} · 타이포 {}/{} 를 시안 스냅샷과 대조 (lineHeight 비율은 타이포 {}개 전부)"
          .format(locked_colors, len(colors), locked_typo, len(typo), len(typo)))

    unverified = lock.get("_unverified", {})
    pending = len(unverified.get("colors", [])) + len(unverified.get("typography", []))
    if pending:
        print("  아직 시안 대조를 못 한 토큰 {}개 — 해당 프레임을 대조하면 스냅샷에 추가한다."
              .format(pending))

    if errors:
        print("\n시안과 어긋난 항목 {}건:".format(len(errors)), file=sys.stderr)
        for e in errors:
            print("  - {}".format(e), file=sys.stderr)
        print("\n값을 고치거나, 시안이 바뀐 것이라면 tools/figma/figma-tokens.lock.json 을 "
              "갱신해라(README 절차).", file=sys.stderr)
        return 1

    print("이상 없음.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
