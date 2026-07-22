# Semantic symbol map

Tên `a`–`k` là symbol đã obfuscate. Alias dưới đây là tên làm việc dựa trên call
site và data flow; chúng không được trình bày như tên gốc của Gameloft.
Overlay hiện tại có 12 class, 31 method và 41 field; đây là working aliases,
không phải tên gốc.

## Class map

| Gốc | Alias đề xuất | Method / field | Vai trò | Confidence |
|---|---|---:|---|---|
| `GloftASBR` | `AssassinsCreedMidlet` | 4 / 3 | MIDlet lifecycle, property `MIDlet-Version`/`HAS-BLOOD`, tạo và restore canvas. | Cao |
| `a` | `SpriteAnimationPlayer` | 14 / 14 | Animation/frame/timer/loop/transform và draw qua sprite `b`. | Cao |
| `b` | `GloftSpriteAndBitmapFont` | 61 / 108 | Parser/render sprite Gameloft, palette/RLE/packed pixels, font mapping/wrap/draw. | Cao |
| `c` | `Waypoint` + `WaypointRegistry` | 6 / 13 | Node ID, tọa độ, wait/speed/next và registry tối đa 400 node. | Cao |
| `d` | `EnemyCombatTables` | 1 / 2 | Bảng chọn AI/animation và damage theo difficulty. | Suy luận |
| `e` | `AudioManager` | 6 / 5 | Load 34 MIDI/WAV slot, single `Player`, play/stop, music/SFX gating. | Cao |
| `f` | `IgpPromotionController` | 49 / 158 | Gameloft IGP 2.1, catalog UI, pointer input, RMS `igp19`, `platformRequest`. | Cao |
| `g` | `PlayerActor` / `EzioController` | 57 / 71 | Kế thừa `i`; movement, combat, parkour, weapon, cheat/free-fly. | Cao |
| `h` | `AudioDurationTable` | 1 / 1 | 34 duration theo millisecond. | Cao |
| `i` | `ActorEntity` | 225 / 206 | Entity base/type dispatcher, fixed-point physics, collision, AI, trigger, attachment, draw. | Cao |
| `j` | `GameCanvasRuntimeAndResourcePackReader` | 88 / 91 | Canvas loop 62 ms, timing/input/math/drawing, multipart pack, LZMA, object/string parser. | Cao |
| `k` | `GameController` | 154 / 337 | Screen FSM, touch, level/entity loader, update/render/camera, UI/dialogue, save, audio, cheat, IGP. | Cao |

## Lifecycle và frame call graph

```text
GloftASBR.startApp()
  ├─ lần đầu: new k(midlet, Display)
  │    ├─ j.<init>()       // Canvas/runtime setup
  │    ├─ k initialization
  │    └─ j.b()            // tạo/start game thread
  │         └─ j.run()
  │              └─ repaint() + serviceRepaints()
  │                   └─ j.paint(Graphics)
  │                        └─ k.a()       // screen/frame dispatcher
  └─ resume: Display.setCurrent(k singleton)

gameplay screen 8/21
  ├─ k.I()                 // world update
  │    └─ for each entity: i.I()
  │         ├─ fixed-point integration/collision
  │         └─ switch i.ax
  │              ├─ player type -> g.e()
  │              ├─ trigger/controller type 10 -> i.aV()
  │              └─ actor-specific handlers
  └─ k.b(false)            // world renderer
       ├─ tile/background layers
       ├─ depth ordering
       └─ i.F() -> b sprite/module renderer
```

`pauseApp()` chỉ gọi `notifyPaused()`. Pause/resume thực tế nằm ở Canvas
`hideNotify()/showNotify()`, `j.c()/j.d()` và override tương ứng trong `k` để dừng
audio/timing. Game loop kết thúc khi screen state `j.c < 0`; transition exit state
11 đặt giá trị âm.

## Alias field quan trọng của `i`/`g`

Các alias sau được xác định bởi read/write pattern trên nhiều handler:

| Symbol | Alias semantic | Kiểu/đơn vị |
|---|---|---|
| `i.ax` | `entityType` | int dispatch type. |
| `i.aw` | `entityId` | ID duy nhất dùng lookup `k.q(id)`. |
| `i.S` | `state` | State/action/animation phụ thuộc entity type. |
| `i.Q` | `previousState` | State trước transition. |
| `i.T` | `animationFrame` | Frame index. |
| `i.U` | `frameTicks` | Tick còn lại/đã trôi của frame. |
| `i.ak`, `i.al` | `worldX`, `worldY` | Tọa độ integer pixels. |
| `i.N`, `i.O` | `fixedX`, `fixedY` | 8.8 fixed-point position. |
| `i.ag`, `i.ah` | `velocityX`, `velocityY` | Fixed-point/tick. |
| `i.ai`, `i.aj` | `accelX`, `accelY` | Fixed-point/tick². |
| `i.P` | `behaviorFlags` | Bitset render/collision/activation. |
| `i.W` | `collisionBounds` | Rectangle world-space dùng overlap. |
| `i.Y` | `secondaryBounds` | Rectangle hit/interaction tùy type. |
| `i.Z` | `typeParameters` | Array tham số record, meaning phụ thuộc `ax/S`. |
| `i.aa` | `sprite` | `b`/`GloftSprite`. |
| `i.av` | `facingLeft` | Boolean hướng; xác nhận từ velocity/render transform. |

`i.i(int)` nên alias là `setState(int)`, không chỉ `setAnimation`: method cập nhật
state, previous state, animation và side effects phụ thuộc type.

`g.e()` là player FSM riêng, lớn và trực tiếp consume input. Không nên hợp nhất
với generic `ActorEntity` handler trong rewrite parity-first.

## Screen state `j.c`

Transition tập trung ở `k.l(int)`; update/render dispatch chính ở `k.a()`.

| State | Alias mạnh nhất |
|---:|---|
| `0` | Staged bootstrap/resource initialization |
| `1` | Title/touch prompt và IGP offer entry |
| `2` | Main menu |
| `3` | Options |
| `4` | Statistics/high-score table |
| `5` | Help pages |
| `6` | About/legal/credits |
| `7` | Không có main case; transition-only/unused |
| `8` | Live gameplay |
| `9` | Staged level loading |
| `10` | Milestone/post-level interstitial |
| `11` | Exit |
| `12`, `13`, `31` | Modal confirmation/message variants |
| `14` | In-game pause menu |
| `15` | Mission-complete score calculation |
| `16` | Không có main case; transition-only/unused |
| `17` | Frozen gameplay render |
| `18` | Title/audio intro |
| `19` | Level selection |
| `20` | Opening story crawl |
| `21` | Gameplay + dialogue/cutscene sub-FSM |
| `22` | Achievement notification |
| `23` | Sound prompt |
| `24` | Ending/credits crawl |
| `25` | IGP invitation |
| `26` | Không có main case; transition-only/unused |
| `27` | IGP catalog |
| `28` | Generic information/confirmation modal |
| `29` | Difficulty selection |
| `30` | Grouped chapter/sequence selector |

Trong state 21, `k.u` là sub-FSM cho dialogue, mission text, timed text, cutscene
control và return-to-play.

## Input symbol map

| Symbol | Alias | Bằng chứng | Confidence |
|---|---|---|---|
| `k.u(mask)` | `isHeld(mask)` | Đọc current bitset. | Cao |
| `k.v(mask)` | `wasPressed(mask)` | Edge press trong frame. | Cao |
| `k.w(mask)` | `wasReleased(mask)` | Edge release. | Cao |
| `k.x(mask)` | `wasRepeatedOrDouble(mask)` | Dựa trên recent/timer state. | Suy luận |

`pointerPressed/Dragged/Released` biến đổi tọa độ portrait thành landscape:

```text
gameX = inputY
gameY = 240 - inputX
```

`j` vẫn chứa keypad/D-pad map legacy, nhưng `k.keyPressed()` và
`k.keyReleased()` là empty override; build này trên thực tế đi theo touch path.

## Rendering symbol map

```text
j.paint(Graphics)
  -> k.a() screen dispatcher
     -> k.b(boolean) world render
        -> tile layers et/ep/eu/er + packed flags
        -> camera k.O/k.P
        -> depth-sort entity arrays
        -> i.F() entity draw
           -> a animation state
           -> b module/frame/palette draw
```

`b` kiêm hai trách nhiệm sprite và bitmap font. Để bảo toàn parity, converter
ban đầu nên giữ một parser chung; runtime mobile có thể tách `SpriteAtlas` và
`BitmapFont` sau khi có golden assets.

## Resource/load symbol map

| Symbol | Alias đề xuất | Vai trò |
|---|---|---|
| `j.a(String)` | `openPack(path)` | Đọc header, part starts và offset table. |
| `j.m(I)I` | `seekPackEntry(index)` | Tìm part/entry đã chọn trong pack hiện tại. |
| `j.n(I)V` | `decodeEntryLengthMarker(index)` | Đọc marker/LZMA flag và tiến cursor entry. |
| `j.e(int)` | `readEntryBytes(index)` | Trả decoded payload. |
| `j.f(int)` | `readTypedEntry(index)` | Parse typed-object tree. |
| `j.a(String,int)` | `loadStringTable(pack,index)` | Parse/materialize string table. |
| `j.g(int)` | `getLoadedString(index)` | Slice/decode UTF-8 string. |
| `j.e()` | `closePack()` | Release pack state/stream. |
| `k.G(int)` | `advanceLevelLoadStage(stage)` | Dialogue/map/script/sprite/entity staged loader. |
| `k.H(int)` | `loadTileLayerAndDimensions(slot)` | Payload + 4-byte width/height companion. |
| `k.I(int)` | `loadLevelLayers(level)` | Map slot 1/4/8/11 và flag companions. |

Do overload/obfuscation, alias phải luôn kèm descriptor khi dùng trong tooling;
ví dụ `j.a(String)` khác hoàn toàn các overload draw/math.

## Audio, save và external boundaries

### Audio

- `e.a(String)` load pack/slot streams.
- `e.a:(IZ)V` là `playAudioSlot(slot, allowRestart)`; `e.b()` stop/close current player.
- Slot `0..9` được gate bởi music option `k.bE`; `10..33` bởi SFX option `k.bF`.
- `h.a[index]` cung cấp duration để mô phỏng completion vì Java ME player API
  không được dùng như timeline authority.

### Save

- `k.e(boolean)` là load/save codec cho RMS store `/ASBR`.
- Record `1` dài đúng 512 byte (`k.bA`).
- `k.bf`, `1000 × 22` byte, là in-memory level-resume snapshot; không đồng nhất
  với RMS file và không sống qua process loss.
- Byte map/reset/corruption contract đầy đủ: [`save-format.md`](./save-format.md).
- IGP dùng RMS store riêng `igp19`.

### External/network

`f` gọi `MIDlet.platformRequest()` khi người dùng chọn quảng cáo. Không có
`Connector`, `HttpConnection`, socket hoặc datagram call trong inventory. Vì vậy
network surface trong JAR là external URL launch qua platform, không phải HTTP
client nhúng.

## Empty hooks và decompiler risk

Empty method quan sát được có vẻ là platform/legacy hook, không phải mất code:

- `a.run()`;
- `b.b(boolean)` và một overload text draw;
- `j.sizeChanged`, một số draw hook, `j.f(Graphics)`, `j.a(Graphics,int)`;
- `k.keyPressed`, `k.keyReleased`;
- `a.d()` gần như scheduler remnant.

Method cần bytecode-level review trước khi translate:

| Nhóm | Method |
|---|---|
| Hard structured failure | `i.aV()`; đã khôi phục riêng. |
| Player central FSM | `g.e()`, `g.ay()`, `g.az()`, `g.n()` |
| Entity central | `i.I()`, `i.F()` và 10 warning-tagged helper |
| Controller central | `k.a()`, `k.l(int)`, `k.D()`, `k.d(boolean)`, `k.b(int,int,int,boolean,boolean)` |
| Type-inference invalid Java | `f.d(int)`, `j.a(InputStream)`, `j.c(int,int)`, `k.R()`, `k.G(int)` |

Full declarations/call graph/field accesses nằm trong
[`inventory/`](../reconstructed-project/inventory/). Tài liệu này chỉ đặt alias
cho boundary và symbol có evidence mạnh, tránh bịa 666 tên semantic không thể
chứng minh.
