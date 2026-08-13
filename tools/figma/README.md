# Figma ↔ 디자인시스템 정합성 검사

시안과 실제 구현이 어긋났는지를 눈이 아니라 **데이터로** 확인하기 위한 도구.

MCP 로 화면을 보고 눈대중으로 `16.dp` 를 적으면, 시안이 20 이었는지 16 이었는지 나중에
아무도 모른다. 여기서 뽑은 원본 JSON 이 그 판단 근거가 된다.

## 준비

```bash
# Figma 설정 > Personal access tokens 에서 발급 (file_content:read 스코프)
export FIGMA_TOKEN='figd_...'
```

토큰은 **환경변수로만** 넘긴다. 스크립트가 인자로 받지 않는 이유는 인자로 주면 셸
히스토리와 `ps` 출력에 그대로 남기 때문이다. 리포에는 어떤 형태로도 커밋하지 않는다.

## STEP 1 — 시안에서 스타일 추출

```bash
# Figma 에서 프레임 우클릭 > Copy link 한 URL 을 그대로 붙여도 된다
python3 tools/figma/extract_figma_tokens.py --node 'https://www.figma.com/design/.../?node-id=2001-3120'

# 노드 id 만 알면
python3 tools/figma/extract_figma_tokens.py --node 2001-3120

# 여러 프레임 한 번에
python3 tools/figma/extract_figma_tokens.py --node 2001-3120,2001-4400
```

결과: `tools/figma/out/figma-tokens-raw.json` (gitignore 대상 — 재생성 가능)

프레임별로 모든 노드의 다음 값이 들어간다.

| 대상 | 뽑는 값 |
| --- | --- |
| TEXT | `fontSize`, `fontWeight`, `lineHeightPx`(단위 변환 포함), `letterSpacing`, `textCase` |
| 레이아웃 노드 | `layoutMode`, `itemSpacing`, `padding*`, `cornerRadius`, `strokeWeight`, 크기 |
| 모든 노드 | 단색 `fills` 의 hex, `styleIds`, `boundVariables` |

`styleIds` 와 `boundVariables` 가 **둘 다 비어 있는 노드**가 핵심이다 — 시안에서조차
토큰을 안 쓰고 손으로 값을 넣은 자리라, 코드에 옮길 때 근거가 없다. 실행 요약이 이 개수를
따로 세어 준다.

## 알려진 함정

- **노드 id 형식**: Figma URL 은 `node-id=1234-5678`(하이픈)인데 REST API 는 `1234:5678`
  (콜론)을 받는다. 안 바꾸면 에러 없이 빈 결과가 나온다. 스크립트가 자동 변환한다.
- **lineHeight 단위**: Figma 가 px 또는 폰트크기 % 로 준다. `Type.kt` 는 sp 절대값이라
  비교하려면 px 가 필요해서, 원본과 계산값을 둘 다 남긴다.
- **게시 스타일 vs 로컬 스타일**: `/v1/files/{key}/styles` 는 팀 라이브러리로 **게시된**
  것만 준다. 게시 안 한 파일이면 비어 있는 게 정상이라, 파일 응답 안의 `styles` 맵을
  주력으로 쓴다.
- **변수 hex 값**: 이 스크립트는 노드에 실제로 칠해진 색을 뽑는다. 변수 자체의 정의값은
  Figma MCP `get_variable_defs` 쪽이 정확하다. 둘이 다르면 MCP 를 믿는다.

## 다음 단계

- STEP 2 — `core/designsystem` 토큰 목록과 대조해 매핑 테이블 생성
- STEP 3 — 프레임별 구현 스펙 문서 생성

현재 `core/designsystem` 에는 `ModyColors`(23개)와 `ModyTypography`(15개)만 있고
**간격 토큰이 없다.** 코드 전체에 dp 리터럴 705개(고유값 63종)가 흩어져 있어서, 간격은
매핑할 대상 자체가 없는 상태다. STEP 2 의 spacing 결과가 전부 `REVIEW_NEEDED` 로 나오는
것은 버그가 아니라 이 현황 그대로다.
