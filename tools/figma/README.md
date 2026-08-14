# 디자인 토큰 정합성 검사

시안(Figma)과 `core:designsystem` 의 토큰이 어긋나면 CI 에서 막는다.

```
Figma ──사람이 MCP로 추출──▶ figma-tokens.lock.json ──CI──▶ check_design_tokens.py
                                (커밋된 스냅샷)              Color.kt / Type.kt 대조
```

## 왜 만들었나

`c2` 의 `lineHeight` 가 19.6sp 여야 하는데 아래 12sp 행에서 복사된 16.8 이 들어가 있었다.
**24곳에서 2.8sp 씩 어긋난 채 코드리뷰를 통과했다.** "살짝 빽빽한데요" 로는 안 걸리는
종류다. 사람이 못 잡으니 CI 가 본다.

## CI 가 Figma 를 호출하지 않는 이유

`check_design_tokens.py` 는 네트워크를 타지 않는다. 커밋된 lock 파일만 읽는다.

- CI 에 Figma 토큰을 넣지 않아도 된다 (시크릿 관리·유출 위험 없음)
- Figma 장애나 네트워크 실패가 빌드를 깨뜨리지 않는다
- 검사 결과가 재현 가능하다 — 같은 커밋이면 언제 돌려도 같은 결과

대가로 **시안이 바뀌면 사람이 lock 을 갱신해야 한다.** 아래 절차다.

---

## lock 파일 갱신 절차

### 0. 도구 선택이 정확도를 가른다

Figma MCP 는 도구마다 리턴하는 게 다르다. **이걸 틀리면 값이 어긋난다.**

| 도구 | 리턴 | lock 갱신에 |
| --- | --- | --- |
| `get_screenshot` | 이미지(PNG) | ❌ **쓰지 마라** |
| `get_variable_defs` | 노드에 바인딩된 변수의 이름·값 (구조화) | ✅ 색·타이포 값의 기준 |
| `get_design_context` | 노드의 스타일·토큰 컨텍스트 (구조화) | ✅ 어떤 토큰이 쓰였는지 |
| `get_metadata` | 정확한 x/y/w/h | ✅ 좌표·간격 대조용 |

스크린샷을 보고 값을 추정하면 오차가 난다 — 안티에일리어싱된 픽셀에서 hex 를 읽거나,
글자 높이를 눈대중하게 된다. `get_variable_defs` / `get_design_context` 는 **Figma 가
들고 있는 원본값을 그대로** 준다. 추정이 개입할 자리가 없다.

lock 파일의 값은 전부 `get_variable_defs` 에서 나왔다 (`_source.method` 참고).

### 1. 시안 파일

```
file key: uQWUtLv8xzOFNrthwozXs9
https://figma.com/design/uQWUtLv8xzOFNrthwozXs9/모디-MODY--export용-
```

구 파일 `eUXrUuSsupAVdKb5xIatWW` 는 폐기됐다 (PPT 페이지만 남음).

### 2. 노드에서 값 뽑기

토큰이 실제로 쓰인 프레임을 골라 호출한다. 지금 lock 에 반영된 노드:

| 노드 | 화면 |
| --- | --- |
| `251:3651` | 걸음 수(walk) |
| `251:2309` | 챌린지 탭 |
| `251:3880` | 주간 챌린지 상세 |

```
get_variable_defs(nodeId: "251:2309")     # 색·타이포 원본값
get_design_context(nodeId: "251:2309")    # 어떤 토큰이 어디에 쓰였는지
get_metadata(nodeId: "251:2309")          # 좌표·크기 (간격 대조할 때)
```

> `9:29`("UI" 캔버스)는 매우 크다. `get_metadata` 를 통째로 부르면 응답이 감당이 안 되니
> 화면 단위 노드로 좁혀서 부른다.

### 3. lock 파일 반영

`figma-tokens.lock.json` 의 형식:

```json
"colors":     { "<Figma 변수명>": { "hex": "FBD406", "code": "<Color.kt 이름>" } },
"typography": { "<Figma 스타일명>": { "size": 14, "weight": 500, "code": "<Type.kt 이름>" } }
```

- `hex` 는 `#` 없이 대문자 6자리 (알파 제외)
- `weight` 는 수치 (`SemiBold` → 600)
- `code` 는 코드 쪽 이름. Figma 명과 다르다 (`Main` → `Primary100`, `Gray1` → `Gray01`)

`_source` 블록도 같이 갱신한다 — `verifiedNodes`, `capturedOn`.

**대조 못 한 토큰은 `_unverified` 에 남긴다.** 빼먹으면 "전부 검증됨"으로 읽힌다.
새 프레임을 대조해 값이 확인되면 `_unverified` 에서 빼고 본문으로 옮긴다.

### 4. 확인 후 커밋

```bash
python3 tools/figma/check_design_tokens.py
```

`이상 없음.` 이면 lock 과 코드가 맞는 것이다. 어긋나면 둘 중 하나를 고친다.

- **코드가 틀렸다** → `Color.kt` / `Type.kt` 수정
- **시안이 바뀐 것이다** → lock 이 정답이므로 코드를 시안에 맞춘다.
  이때 그 토큰을 쓰는 화면이 전부 바뀌므로 영향 범위를 PR 에 적는다
  (`c2` 하나가 24곳이었다)

---

## 검사 내용

### 1. 스냅샷 대조

lock 에 박힌 색·타이포를 `Color.kt` / `Type.kt` 와 비교한다. 정규식으로 파싱하므로
**토큰 선언 형식을 바꾸면 스크립트도 같이 고쳐야 한다.**

```kotlin
val Primary0 = Color(0xFFFBD406)
val c2: TextStyle = modyTextStyle(FontWeight.Medium, 14.sp, 19.6.sp)
```

색은 알파도 본다 — 팔레트는 전부 `FF` 여야 한다. 알파가 끼면 화면에서 색이 흐려진다.

### 2. lineHeight 비율 규칙

시안 전체가 `lineHeight = fontSize × 1.4` 다. 이 규칙은 **lock 에 없는 토큰까지 전부**
적용한다.

스냅샷은 대조한 프레임만 커버하지만(현재 타이포 8/15), 이 규칙은 아직 안 본 토큰도
잡는다. **`c2` 를 잡은 게 이 규칙이다.**

---

## 나중에: lock 갱신 자동화

지금은 사람이 MCP 로 뽑아 커밋한다. 스크립트로 자동화하려면 Figma REST API 를 쓴다.

| 시안이 쓰는 것 | 엔드포인트 |
| --- | --- |
| Variables | `GET /v1/files/:file_key/variables/local` |
| Styles (클래식) | `GET /v1/files/:file_key/nodes?ids=...` → 각 노드의 `style` / `styles` / `boundVariables` |

이러면 "시안 바뀌면 사람이 갱신" 제약이 사라진다. 다만 **CI 가 Figma 토큰을 갖게 되므로**,
갱신 스크립트는 CI 가 아니라 로컬/별도 워크플로에서 돌리고 결과를 PR 로 올리는 형태가
낫다 — 검사 CI 는 지금처럼 토큰 없이 스냅샷만 읽게 둔다.
