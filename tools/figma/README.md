# Figma ↔ 디자인시스템 정합성 검사

시안과 실제 구현이 어긋났는지를 눈이 아니라 **데이터로** 확인하기 위한 도구.

MCP 로 화면을 보고 눈대중으로 `16.dp` 를 적으면, 시안이 20 이었는지 16 이었는지 나중에
아무도 모른다. 여기서 뽑은 원본 JSON 이 그 판단 근거가 된다.

## 준비

```bash
export FIGMA_TOKEN='figd_...'
```

발급: Figma 웹 → 우상단 아바타 → **Settings** → **Security** 탭 → *Personal access tokens*
→ `Generate new token`. 스코프는 **File content: Read-only** 만 준다. 생성 직후 한 번만
보여주므로 그 자리에서 복사한다.

무료(Starter) 플랜에서도 발급되고 이 스크립트가 쓰는 엔드포인트(`/files/{key}/nodes`)도
동작한다. 유료가 필요한 건 **변수(Variables) REST API** 와 **팀 라이브러리 게시 스타일**
쪽이라, 이 스크립트는 둘 다 없어도 돌아가게 만들었다 — 게시 스타일 조회가 실패하면 경고만
남기고 파일 응답 안의 로컬 스타일로 넘어간다.

토큰은 **환경변수로만** 넘긴다. 스크립트가 인자로 받지 않는 이유는 인자로 주면 셸
히스토리와 `ps` 출력에 그대로 남기 때문이다. 리포에는 어떤 형태로도 커밋하지 않는다.

## STEP 1 — 시안에서 스타일 추출

```bash
# 프레임 우클릭 > Copy link 한 URL 을 그대로 붙인다 (권장)
python3 tools/figma/extract_figma_tokens.py --node 'https://www.figma.com/design/uQWU.../모디-MODY?node-id=9-29'

# 여러 프레임 한 번에 (같은 파일이어야 한다)
python3 tools/figma/extract_figma_tokens.py --node '<URL1>,<URL2>'

# 노드 id 만 줄 거면 file key 를 따로 넘겨야 한다
python3 tools/figma/extract_figma_tokens.py --file-key uQWUtLv8xzOFNrthwozXs9 --node 9-29
```

**file key 를 코드에 박아두지 않는다.** 시안 파일이 작업용/export용으로 나뉘어 있어서,
기본값을 두면 엉뚱한 파일을 보고도 정상 동작한 것처럼 보인다. URL 을 통째로 주면 거기서
읽으므로 어긋날 일이 없다.

**요청한 노드가 하나라도 응답에 없으면 아무것도 쓰지 않고 exit 1 이다.** 같은 이유다 —
부분 결과를 써두면 빠진 프레임이 조용히 lock 밖으로 나가고, "대조했다"고 믿는 범위와
실제 범위가 갈라진다. 노드 id 가 낡았거나(프레임 삭제·재생성) 다른 파일의 id 인 경우니
Copy link 로 URL 을 다시 받아 부른다.

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

## 토큰 드리프트 검사 (CI)

```bash
python3 tools/figma/check_design_tokens.py
```

`figma-tokens.lock.json`(시안 스냅샷)과 `Color.kt` / `Type.kt` 를 대조해 어긋나면 종료 코드 1.
CI 의 `Check design tokens` 스텝이 PR 마다 돌린다.

**Figma 를 호출하지 않는다.** 커밋된 스냅샷만 읽으므로 CI 에 Figma 토큰을 넣을 필요가 없고,
Figma 장애나 레이트 리밋에 CI 가 흔들리지 않는다. 대신 시안이 바뀌면 사람이 스냅샷을
갱신해 커밋해야 한다 — 그 변경이 PR diff 에 남는 것이 오히려 이 방식의 장점이다.

검사 두 가지:

1. **스냅샷 대조** — 잠근 색 16개, 타이포 8개의 hex·fontSize·weight. 팔레트에 알파가
   섞이는 것도 막는다.
2. **비율 규칙** — 시안 전체가 `lineHeight = fontSize × 1.4` 다. 스냅샷에 없는 토큰까지
   **타이포 15개 전부**에 적용한다. 실제로 `c2` 를 잡아낸 것이 이 규칙이다.

### 시안이 바뀌었을 때

값을 코드에 맞추는 게 아니라 **스냅샷을 갱신**한다.

1. 바뀐 프레임에서 토큰을 다시 뽑는다 (위 STEP 1, 또는 Figma MCP `get_variable_defs`)
2. `figma-tokens.lock.json` 의 해당 항목과 `_source.capturedOn` 을 고친다
3. 코드 토큰도 함께 고치고 한 PR 로 올린다 — 스냅샷만 고치면 검사는 통과하지만 화면은
   그대로다

## 다음 단계

- STEP 2 — `core/designsystem` 토큰 목록과 대조해 매핑 테이블 생성
- STEP 3 — 프레임별 구현 스펙 문서 생성

현재 `core/designsystem` 에는 `ModyColors`(23개)와 `ModyTypography`(15개)만 있고
**간격 토큰이 없다.** 코드 전체에 dp 리터럴 705개(고유값 63종)가 흩어져 있어서, 간격은
매핑할 대상 자체가 없는 상태다. STEP 2 의 spacing 결과가 전부 `REVIEW_NEEDED` 로 나오는
것은 버그가 아니라 이 현황 그대로다.
