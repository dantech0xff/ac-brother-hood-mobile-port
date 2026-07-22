# Khôi phục method `i.aV()`

`i.aV()` là hard failure duy nhất của structured decompiler. Tài liệu này chứng
minh method không bị mất và mô tả hành vi đã khôi phục bằng simple source,
fallback và exact bytecode. Không có phân tích động.

## Danh tính và coverage

| Thuộc tính | Giá trị |
|---|---|
| Member | `private void defpackage.i.aV()` |
| JVM descriptor | `()V` |
| Flags | `ACC_PRIVATE` |
| Stack / locals / args | `6 / 4 / 1` |
| Bytecode range | `0..7879`, dài 7.880 byte |
| Exception/debug table | Không có exception, line-number hoặc local-variable table |
| Simple source | [`src/simple/i.java`](../reconstructed-project/src/simple/i.java), dòng 11.800–13.181 |
| Fallback | [`src/fallback/i.java`](../reconstructed-project/src/fallback/i.java), dòng 32.830–36.892 |
| Exact bytecode | [`bytecode/i.javap.txt`](../reconstructed-project/bytecode/i.javap.txt), bắt đầu dòng 39.788 |

Fallback label biểu diễn hexadecimal bytecode offset; ví dụ `L5ac = 0x5ac =
1452`, `L1ec7 = 0x1ec7 = 7879`.

Method chỉ được gọi một lần: `i.I()` dispatch entity `ax == 10`. Vì vậy `aV()`
là update handler mỗi tick cho nhóm entity trigger/controller type 10; field `S`
chọn hành vi cụ thể. Không nên gọi toàn bộ method là “enemy AI”: nhiều state là
trigger map, interaction zone, scripted encounter hoặc minigame controller.

## Top-level dispatch chính xác

```text
S= 0 -> 1452    1 -> 7879    2 -> 5025    3 -> 5287
   4 -> 5561    5 -> 5608    6 -> 5678    7 -> 5699
   8 -> 5419    9 -> 5720   10 ->  792   11 -> 7879
  12 -> 5980   13 -> 6008   14 -> 5721   15 -> 7879
  16 -> 6013   17 -> 6980   18 -> 7320   19 -> 5560
  20 -> 7879   21 -> 6229   22 -> 5501   23 -> 6281
  24 ->  542   25 -> 7879   26 -> 7879   27 -> 7879
  28 -> 6823   29 -> 4836   30 -> 2674   31 -> 1514
  32 -> 6353   33 -> 6434   34 -> 1513   35 -> 7879
  36 -> 6537   37 -> 7879   38 -> 7879   39 -> 6658
  40 -> 7879   41 -> 6659   42 -> 7541   43 -> 6697
  44 -> 6745   45 -> 6777   46 ->  481   47 ->  537
  48 ->  358   49 ->  444   50 -> 7562   51 -> 7751
  52 -> 7879   53 -> 7801   54 ->  310   55 ->  244
default -> 7879
```

Các state direct-return/no-op trong build này: `1, 9, 11, 15, 19, 20, 25, 26,
27, 34, 35, 37, 38, 39, 40, 52` và default.

## Semantic theo state

| `S` | Hành vi khôi phục |
|---:|---|
| `0` | Khi player overlap và trigger `v()` hợp lệ, publish `p/aG` vào `k.af/k.ag`; nếu không thì clear. |
| `2` | Conditional flag-enabler. Qua overlap/dependency gates, OR `Z[3]` vào `P` của target `q(Z[1])`, propagate một số flag rồi xóa trigger. |
| `3` | Flag-disabler đối xứng state 2; clear `Z[3]` trên target và xóa trigger. |
| `4` | Vùng text contextual: đặt `k.aB = k.d(k.aj+1,aF)`; clear sau khi rời vùng và timer hết. |
| `5` | Interaction zone nhận mask action `16388` hoặc direction theo facing; chuyển player state 22. |
| `6` / `7` | Overlap đặt `k.aZ=true/false`. |
| `8` | Toggle static `bn`, thay đổi player state 79/80 và `g.z`, rồi xóa trigger. |
| `10` | Scripted vertical encounter controller; xem pseudocode bên dưới. |
| `12` | Overlap gọi `k.n(aF)` rồi xóa trigger. |
| `13` | Xóa trigger vô điều kiện. |
| `14` | Detach/launch interaction với object `ax==69`; reset bit/motion, có thể launch player `±3328,-6656` ở state 243. |
| `16` | Multi-stage interaction; tạo helper type 105, quản lý attachment và action state 284. |
| `17` | Stateful launcher/trampoline: bắt player, khóa state 297, sau input thì phóng và đợi recovery. |
| `18` | Khi overlap, kích hoạt object `ax=4,S=33`, đặt ngoài `k.ac`, speed `±p`, state phase `aA=1`. |
| `21` | Gọi `k.b(8,k.aj+1,aF,p)`, có thể chuyển screen 21; đặt `k.x=48`, xóa trigger. |
| `22` | Trigger navigation/action: `aF<0 -> k.w`, `aF==2 -> k.z(2)`, còn lại `k.A(aF)`. |
| `23` | Đồng bộ flag `cq` của linked entity `ax==11` theo overlap và `P()`. |
| `24` | Context attach interaction; action mask gắn player, state 267, giữ helper effect type 7. |
| `28` | Latched overlap zone dùng player `aA` bit 8, `g.z` và optional `az`. |
| `29` | Delayed target-unlocker: đợi các linked object kết thúc rồi clear behavior flags và xóa trigger. |
| `30` | Enemy/wave-grid controller; xem pseudocode bên dưới. |
| `31` | Minigame bốn symbol/input; xem pseudocode bên dưới. |
| `32` | Overlap attach player vào trigger, state 38; snap vertical position khi attached. |
| `33` | Area effect publish `g.B`, slope `g.l`, `g.A`; vô hiệu ở player states 148–150. |
| `36` | Contextual zone publish center/bottom/optional parameter vào `g.n/o/k/d`, quản lý ownership và flag 16. |
| `41` | Xóa trigger nếu special actor `g.a` type 51 overlap. |
| `42` | Xóa trigger khi player overlap. |
| `43` | Nếu player state 60/61 overlap, chuyển state 203. |
| `44` | Overlap đặt `k.W=k.V-aE`, xóa trigger. |
| `45` | Map mode 3 chuyển player state 34; mode khác chuyển screen 12 và xóa trigger. |
| `46` | Ownership zone cho `g.q/g.d`; clear chỉ khi trigger hiện tại vẫn là owner. |
| `47` | Đặt `k.aQ=null`. |
| `48` | Boss speed trigger: target `q(o)` overlap thì gán `target.aG=p`, tạo debug string rồi xóa trigger. |
| `49` | Target overlap chuyển target state 20 và xóa trigger. |
| `50` | Interaction với `g.a` type 43; helper type 7, action launch player state 243, restore `g.C`. |
| `51` | Duy trì singleton overlap owner `i.cv`. |
| `53` | Nếu `g.D` và player state `0/1/5` overlap: state 360, zero velocity, xóa trigger. |
| `54` | Nếu target state 29 và overlap, chuyển target state 30 và xóa trigger. |
| `55` | Nếu `k.aU` state 13 overlap, chuyển state 25, copy X, zero velocity. |

## Ba state phức tạp

### `S == 10`: scripted vertical encounter

Pseudocode rút gọn nhưng giữ thứ tự state effect:

```java
if (be) {
    remove(this);
    return;
}

int gap = player.W[1] - W[3];
if (gap > Z[1] && gap < Z[0]) {
    if (bB) return;
    beginScriptMode();
    ensureEffectNearCameraCenter();
    movePlayerToCameraCenter();
    if (actionPressed() || player.V()) {
        player.G();
        bB = true;
        bF = bG = -1;
        endBlockingMode();
        if (player.S != 4) player.setState(4);
        bi = true;
        player.az = 199;
    }
    return;
}

if (overlap(player.W, W)) {
    if (bB && bF >= Z[2] && bF <= Z[3])
        ensureState35EffectAtPlayerMinus85();
    else {
        player.G();
        player.createEffect(71, player.ak, player.al - 85);
        be = true;
        player.setState(34);
    }
    return;
}

cleanupState39EffectIfPresent();
if (bB && bi && bF == -1) {
    player.setState(28);
    bC = bD = false;
    bF = 100;
    bE = Z[4];
    k.A(25);
}
if (gap > 0 && gap < Z[0]) {
    if (bF >= Z[2] && bF <= Z[3]) ensureState35EffectAtPlayerMinus85();
    return;
}
if (player.ae != null) player.G();
if (gap < 0) {
    bi = false;
    remove(this);
    if (player.S == 26 || player.S == 28 || player.S == 29) player.setState(27);
}
k.p();
```

### `S == 30`: wave-grid controller

- Overlap bật `P|=16`.
- `Z[1]` là số row, `Z[2]` là actor/row; mode `Z[6]==3` ép `1×3`.
- Lần đầu allocate `cr[row][col]`, tạo child type/state theo mode `0..3`, gán
  health table, flags, bounds và vị trí. Chỉ row 0 được register ban đầu.
- Normal mode đợi mọi child của row hiện tại báo hoàn tất rồi register row kế.
- Special mode respawn actor đã hoàn tất theo cooldown `aC=Z[7]`.
- Khi mọi điều kiện xong, unlock linked target `q(Z[0])`, clear flag và xóa
  controller.

Các oddity phải giữ khi translate:

```text
child.aw = 5000 + row * rowCount + col   // bytecode dùng rowCount, không dùng columnCount
mode 3 ghi this.Z[5], không phải child.Z[5]
chỉ state 30 có đường aS(); goto dispatchStart để redispatch trong cùng tick
```

### `S == 31`: four-symbol input minigame

- Khi overlap, đóng/cancel modal cũ, bật input/minigame globals và decode `Z[1]`
  thành bốn nibble high-to-low.
- Tạo tối đa bốn `SpriteAnimationPlayer` trong static `bA`.
- `Z[2]` là timeout mỗi attempt; `Z[3]` là completion state/target;
  `Z[4]` khởi tạo progress.
- Input đúng advance symbol; navigation/cancel đánh dấu attempt failed bằng
  `m += 10`; timeout cũng đi nhánh fail.
- Completion clear toàn bộ sprite/input state, có thể activate linked entity và
  chuyển screen/cutscene theo map mode.

Bytecode gọi `h(k.s(aA))` hai lần liên tiếp ở một nhánh completion; chưa có bằng
chứng đây là lỗi nên bản rewrite parity-first phải giữ hoặc đặt dưới regression
test, không tự ý “dọn code”.

## Dependency surface

Method đọc/ghi gần như toàn bộ contract của type-10 entity:

- Core entity: `S`, `P`, `W/Y`, `Z/X`, `ak/al`, fixed-point motion, sprite/effect,
  linked IDs `o/p/aG`, latch/timer fields `aA..aE`.
- Static `i`: encounter/minigame arrays và flags `bA..bG`, `bn`, `cu`, `cv`,
  combat health tables.
- Player/global `g`: special actor pointers, contextual zones, input gating và
  player state/motion/effect methods.
- Controller `k`: player singleton, entity lookup/register/remove, camera/map
  globals, screen transition, input mask và UI/script helpers.

Chi tiết call/field access từng bytecode offset có thể truy vấn trong
[`calls.json`](../reconstructed-project/inventory/calls.json) và
[`field-accesses.json`](../reconstructed-project/inventory/field-accesses.json).

## Khuyến nghị dịch thành Java/Kotlin có cấu trúc

Có thể viết replacement compile được, nhưng không nên refactor tự do ngay:

1. Dùng labeled `while` + `switch(S)` để giữ đường redispatch của state 30.
2. Translate state 10, 30, 31 cơ học từ simple/fallback và kiểm từng state write.
3. Giữ evaluation/null-check order cùng các oddity đã nêu.
4. Tạo static fixtures cho mỗi state và snapshot expected mutation.
5. Chỉ tách helper sau khi bản parity-first qua kiểm tra branch/effect.

Không thể phục hồi tên local, comment hoặc ý định thiết kế ban đầu vì class không
có debug table. Tuy nhiên không còn unresolved bytecode behavior ở cấp top-level
dispatch; phần chưa biết chủ yếu là tên domain chính xác của `S` và từng vị trí
trong `Z[]`.
