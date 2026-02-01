# 极性槽系统设计文档

## 🎯 设计目标

实现Warframe风格的极性槽系统，让玩家可以配置每个MOD槽位的极性，从而：
- 与MOD极性匹配获得50%容量折扣
- 提高配装的策略性和自定义性
- 支持通用极性(*)与任何极性匹配

---

## 📊 数据结构设计

### 1. 极性槽存储结构

**文件**: `src/main/kotlin/com/skillview/core/mod/SlotPolarityManager.kt` (新增)

```kotlin
// 极性槽管理器
object SlotPolarityManager {
    
    // 用户界面中显示极性值的位置
    data class PolaritySlotConfig(
        val weaponSlots: MutableMap<Int, String> = mutableMapOf(),    // 武器MOD: 槽位索引 -> 极性符号
        val playerSlots: MutableMap<Int, String> = mutableMapOf(),    // 角色MOD: 槽位索引 -> 极性符号
        val skillSlots: MutableMap<Int, String> = mutableMapOf()      // 技能MOD: 槽位索引 -> 极性符号
    )
    
    // 持久化到玩家数据
    fun saveSlotPolarities(player: Player, config: PolaritySlotConfig)
    
    // 从玩家数据加载
    fun loadSlotPolarities(player: Player): PolaritySlotConfig
}
```

### 2. WeaponModLoadout扩展

**当前状态**:
```kotlin
data class WeaponModLoadout(
    val weaponItem: ItemStack? = null,
    val mods: MutableMap<Int, ItemStack> = mutableMapOf(),
    val slotPolarities: MutableMap<Int, String> = mutableMapOf()  // ✅ 已有字段
)
```

**需要实现**:
- 在菜单中显示和修改 `slotPolarities`
- 在保存/加载时持久化这个字段

### 3. PlayerModLoadout 和 SkillModLoadout 扩展

需要为这两个类添加 `slotPolarities` 字段：

```kotlin
data class ModLoadout(
    var isCapacityUpgraded: Boolean = false,
    val mods: MutableMap<Int, ItemStack> = mutableMapOf(),
    val slotPolarities: MutableMap<Int, String> = mutableMapOf()  // 新增
)
```

---

## 🎮 UI设计

### 武器MOD极性槽菜单

**菜单布局** (6行 9列):
```
#########
##M#W#M##
##M#P#M##    P = 极性槽配置按钮
#########
###C#S###
#########
```

**交互流程**:
1. 玩家点击 `P` 按钮
2. 打开极性槽配置界面
3. 显示6个MOD槽位的极性设置

### 极性槽配置界面

**布局** (4行 9列):
```
#########
#S0#P0#X0#   S=槽位0, P=极性, X=清除
#S1#P1#X1#
#S2#P2#X2#   依次类推...
```

**交互**:
- 点击 `S` 显示该槽位当前极性
- 点击 `P` 显示极性选择菜单
- 点击 `X` 清除该槽位极性(设为"无")

### 极性选择菜单

**布局** (3行 9列):
```
#########
#V#D#-#*#    V=红(Madurai), D=蓝(Vazarin), -=绿(Naramon), *=通用
#=#R#Y#无#   =蓝紫(Zenurik), R=橙(Unairu), Y=粉(Penjaga), 无=清除
```

---

## 📝 实现步骤

### Step 1: 创建 SlotPolarityManager (核心管理器)

负责:
- 加载/保存极性槽配置
- 从玩家NBT数据中序列化/反序列化

### Step 2: 为 SkillStorage.ModLoadout 添加 slotPolarities 字段

修改 `SkillStorage.kt`:
```kotlin
data class ModLoadout(
    var isCapacityUpgraded: Boolean = false,
    val mods: MutableMap<Int, ItemStack> = mutableMapOf(),
    val slotPolarities: MutableMap<Int, String> = mutableMapOf()  // 新增
)
```

### Step 3: 修改武器MOD菜单

文件: `WeaponMod.kt`

添加:
1. 极性槽配置按钮 (`P`)
2. 打开极性配置菜单的逻辑
3. 在保存武器配置时保存极性信息

### Step 4: 修改角色MOD菜单

文件: `PlayerMod.kt`

添加:
1. 极性槽配置按钮
2. 极性配置菜单
3. 保存极性信息到 `ModLoadout.slotPolarities`

### Step 5: 修改技能MOD菜单

文件: `SkillMod.kt`

添加:
1. 极性槽配置按钮
2. 极性配置菜单
3. 将极性信息保存到技能书NBT

### Step 6: 更新 CapacitySystem

确保容量计算使用正确的 `slotPolarities`:

```kotlin
fun calculateUsedCapacity(
    inventory: Inventory,
    modSlots: List<Int>,
    slotPolarities: Map<Int, String> = emptyMap()  // 已支持
): Int {
    // 已经正确实现，使用 slotPolarities 参数
}
```

---

## 🎨 极性显示颜色

| 极性 | 符号 | 颜色代码 | 示例 |
|------|------|---------|------|
| Madurai | V | &c | &c[V] |
| Vazarin | D | &b | &b[D] |
| Naramon | - | &a | &a[-] |
| Zenurik | = | &9 | &9[=] |
| Unairu | R | &6 | &6[R] |
| Penjaga | Y | &d | &d[Y] |
| Universal | * | &f | &f[*] |
| None | 无 | &7 | &7[无] |

---

## 💾 数据持久化

### 武器MOD

存储位置: `Player#NBT["weapon_mod_loadout"]`

JSON格式:
```json
{
  "weaponItem": null,
  "mods": { "0": {...}, "1": {...} },
  "slotPolarities": { "0": "V", "1": "D", "2": "-" }
}
```

### 角色MOD

存储位置: `Player#NBT["mod_loadout"]`

JSON格式:
```json
{
  "isCapacityUpgraded": false,
  "mods": { "0": {...}, "1": {...} },
  "slotPolarities": { "0": "V", "1": "*" }
}
```

### 技能MOD

存储位置: 技能书NBT

```
技能书NBT:
  ├─ 技能书基础属性
  │  └─ 技能id: "xxx"
  ├─ 技能MOD
  │  ├─ 槽位0
  │  │  ├─ 极性: "V"
  │  │  └─ MOD: {...}
  │  └─ 槽位1
  │     ├─ 极性: "D"
  │     └─ MOD: {...}
```

---

## ⚙️ 配置示例

### 武器配置示例

```
武器: 长剑
MOD配置:
  槽位0: [V] 伤害MOD (消耗: 10, 极性匹配: 5)
  槽位1: [D] 防御MOD (消耗: 8, 极性匹配: 4)
  槽位2: [-] 攻速MOD (消耗: 12, 极性不匹配: 15)
  
总容量: 5 + 4 + 15 = 24/60
```

### 角色配置示例

```
角色MOD配置:
  槽位0: [V] 生命力MOD (消耗: 20, 极性匹配: 10)
  槽位1: [*] 暴击MOD (消耗: 18, 通用匹配: 9)
  槽位2: [无] 速度MOD (消耗: 12, 无极性: 12)

总容量: 10 + 9 + 12 = 31/60
```

---

## 🧪 测试场景

1. **基本极性匹配**
   - MOD极性V, 槽位极性V -> 消耗50% ✅
   
2. **通用极性**
   - MOD极性*, 任何槽位 -> 消耗50% ✅
   
3. **极性不匹配**
   - MOD极性V, 槽位极性D -> 消耗125% ✅
   
4. **无极性槽**
   - MOD极性V, 槽位极性无 -> 原价消耗 ✅
   
5. **容量计算**
   - 正确应用极性折扣后计算总容量 ✅

---

## 📦 版本规划

- **v1.5.3.0**: 实现完整极性槽系统
  - 所有菜单支持极性槽配置
  - 正确的容量计算和持久化
  - 完整的测试覆盖

---

## 🔮 未来扩展

1. **极性槽升级**
   - 花费资源升级槽位数量
   - 多个同极性槽位
   
2. **Orokin反应堆支持**
   - 容量翻倍
   - 极性配置不丢失
   
3. **极性槽预设**
   - 保存多个配置预设
   - 一键切换配装方案
   
4. **极性分析工具**
   - 推荐最优极性配置
   - 显示容量优化提示

---
