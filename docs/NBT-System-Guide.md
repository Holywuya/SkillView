# 技能书与MOD系统 - NBT数据结构文档

## 目录
1. [技能书 (SkillBook) NBT结构](#技能书-skillbook-nbt结构)
2. [MOD系统 NBT结构](#mod系统-nbt结构)
3. [实际示例](#实际示例)
4. [API使用指南](#api使用指南)
5. [常见操作](#常见操作)

---

## 技能书 (SkillBook) NBT结构

### 1. 基础信息层

技能书的最顶层属性，用于标识技能书的基本信息。

| 键名 | NBT路径 | 数据类型 | 含义 | 示例 |
|------|--------|--------|------|------|
| 类型 | `类型` | String | 物品类型标识 | `skill_book` |
| 品质 | `品质` | String | 品质等级 | `普通`、`精良`、`稀有`、`史诗` |

**示例：**
```yaml
# NBT数据
类型: "skill_book"
品质: "稀有"
```

---

### 2. 基础属性 (ROOT_BASIC)

存储技能书的核心配置信息。所有基础属性均位于 `技能书基础属性` 节点下。

#### 2.1 技能标识

| 键名 | NBT路径 | 数据类型 | 含义 | 示例 |
|------|--------|--------|------|------|
| 技能ID | `技能书基础属性.技能id` | String | 唯一标识符 | `fireball`、`heal` |
| 等级 | `技能书基础属性.等级` | Integer | 技能当前等级 | `1`～`10` |

**示例：**
```yaml
技能书基础属性:
  技能id: "fireball"
  等级: 5
```

**代码访问：**
```kotlin
val skillId = skillBook.getDeepString(NbtPaths.SkillBook.SKILL_ID, "")  // 返回: "fireball"
val level = skillBook.getDeepInt(NbtPaths.SkillBook.LEVEL, 0)           // 返回: 5
```

---

### 3. 属性强化 (ROOT_MODIFIER)

存储技能书从MOD系统获得的属性加成。所有属性强化均位于 `技能书属性强化` 节点下。

#### 3.1 属性强化表

| 键名 | NBT路径 | 数据类型 | 含义 | 取值范围 | 示例 |
|------|--------|--------|------|---------|------|
| 技能倍率 | `技能书属性强化.技能倍率` | Double | 技能伤害倍率 | 0.0～100.0 | `1.5` (150%) |
| 最终伤害 | `技能书属性强化.最终伤害` | Double | 最后应用的伤害乘数 | 0.0～500.0 | `2.0` (加倍伤害) |
| 伤害加成 | `技能书属性强化.伤害加成` | Double | 伤害百分比加成 | 0.0～1000.0 | `50.0` (+50%) |
| 额外范围 | `技能书属性强化.额外范围` | Double | 技能作用范围增加值 | 0.0～100.0 | `10.5` (增加10.5格) |
| 技能强度 | `技能书属性强化.技能强度` | Double | 技能效果强度倍率 | 0.5～2.0 | `1.2` (强度+20%) |
| 冷却缩减 | `技能书属性强化.冷却缩减` | Double | 冷却时间缩减 (可为负) | -100.0～100.0 | `20.0` (-20秒) |
| 魔力减耗 | `技能书属性强化.魔力减耗` | Double | 技能魔力消耗减少 (可为负) | -100.0～100.0 | `10.0` (-10点魔力) |

**属性强化计算规则：**
- **正值** = 增益效果 (通常来自绿色MOD)
- **负值** = 负面效果 (通常来自红色/"堕落"MOD)
- **最终伤害** 应用于所有伤害计算之后 (类似Warframe的"More"乘数)

**示例：**
```yaml
技能书属性强化:
  技能倍率: 1.5
  最终伤害: 2.0
  伤害加成: 50.0
  额外范围: 10.5
  技能强度: 1.2
  冷却缩减: 20.0
  魔力减耗: 10.0
```

**代码访问：**
```kotlin
val multiplier = skillBook.getDeepDouble(NbtPaths.SkillBook.MULTIPLIER, 1.0)
val damageMore = skillBook.getDeepDouble(NbtPaths.SkillBook.DAMAGE_MORE, 1.0)
val damageBonus = skillBook.getDeepDouble(NbtPaths.SkillBook.DAMAGE_BONUS, 0.0)
val extraRange = skillBook.getDeepDouble(NbtPaths.SkillBook.EXTRA_RANGE, 0.0)
val skillPower = skillBook.getDeepDouble(NbtPaths.SkillBook.SKILL_POWER, 1.0)
val cooldownReduction = skillBook.getDeepDouble(NbtPaths.SkillBook.COOLDOWN_REDUCTION, 0.0)
val manaReduction = skillBook.getDeepDouble(NbtPaths.SkillBook.MANA_REDUCTION, 0.0)
```

---

### 4. MOD系统 (MOD_SLOTS)

技能书可以装备最多4个MOD，存储在 `Mod系统` 节点下。

#### 4.1 MOD插槽结构

| 键名 | NBT路径 | 数据类型 | 含义 | 说明 |
|------|--------|--------|------|------|
| 插槽.0 | `Mod系统.插槽.0` | CompoundTag | 第一个MOD插槽 | 可为空 |
| 插槽.1 | `Mod系统.插槽.1` | CompoundTag | 第二个MOD插槽 | 可为空 |
| 插槽.2 | `Mod系统.插槽.2` | CompoundTag | 第三个MOD插槽 | 可为空 |
| 插槽.3 | `Mod系统.插槽.3` | CompoundTag | 第四个MOD插槽 | 可为空 |

**示例：**
```yaml
Mod系统:
  插槽:
    0: <MOD物品的NBT数据>
    1: <MOD物品的NBT数据>
    2: null  # 空插槽
    3: null  # 空插槽
```

**访问插槽MOD：**
```kotlin
// 格式: "Mod系统.插槽.{slotIndex}"
val slot0Mod = skillBook.getDeepString("${NbtPaths.SkillBook.MOD_SLOT_FORMAT}.0.Mod.id", "")
```

---

## MOD系统 NBT结构

### 1. 基础信息层

| 键名 | NBT路径 | 数据类型 | 含义 | 示例 |
|------|--------|--------|------|------|
| 类型 | `类型` | String | 物品类型标识 | `mod` |
| 品质 | `品质` | String | MOD品质等级 | `普通`、`精良`、`稀有`、`史诗` |

**品质等级对应表：**

| 品质 | 容量消耗倍率 | 升级成本 | 应用场景 |
|------|-----------|--------|--------|
| 普通 | 1.0x | 低 | 基础MOD |
| 精良 | 1.2x | 中 | 常用MOD |
| 稀有 | 1.5x | 较高 | 强力MOD |
| 史诗 | 2.0x | 高 | 顶级/复合MOD |

**示例：**
```yaml
类型: "mod"
品质: "稀有"
```

---

### 2. MOD标识信息

| 键名 | NBT路径 | 数据类型 | 含义 | 示例 |
|------|--------|--------|------|------|
| MOD等级 | `Mod.等级` | Integer | 当前强化等级 | `0`～`10` |
| MOD ID | `Mod.id` | String | 唯一标识符 | `serration`、`streamline` |

**示例：**
```yaml
Mod:
  等级: 5
  id: "streamline"
```

**代码访问：**
```kotlin
val modLevel = mod.getModLevel()         // 返回: 5
val modId = mod.getModId()               // 返回: "streamline"
```

---

### 3. MOD属性 (ROOT_MOD)

存储MOD的属性数据。所有MOD属性均位于 `Mod属性` 节点下。

#### 3.1 核心属性

| 键名 | NBT路径 | 数据类型 | 含义 | 取值范围 | 说明 |
|------|--------|--------|------|---------|------|
| 消耗 | `Mod属性.消耗` | Integer | 装备该MOD的容量消耗 | 1～50 | 与品质和等级相关 |
| 伤害加成 | `Mod属性.伤害加成` | Double | 伤害百分比加成 | -100.0～100.0 | 正数增益,负数削弱 |
| 最终伤害 | `Mod属性.最终伤害` | Double | 最终伤害乘数 | 0.0～500.0 | 最后应用的倍数 |
| 额外范围 | `Mod属性.额外范围` | Double | 技能范围增加 | -100.0～100.0 | 正数增加,负数减少 |
| 技能强度 | `Mod属性.技能强度` | Double | 技能效果强度 | 0.5～3.0 | 技能伤害/治疗倍率 |
| 技能效率 | `Mod属性.技能效率` | Double | 魔力消耗减少 | -100.0～100.0 | 负值为副作用 |
| 冷却缩减 | `Mod属性.冷却缩减` | Double | 冷却时间缩减 | -100.0～100.0 | 负值为副作用 |
| 魔力恢复 | `Mod属性.魔力恢复` | Double | 魔力回复速度 | 0.0～100.0 | 每秒回复量 |
| 魔力上限 | `Mod属性.魔力上限` | Double | 最大魔力值增加 | 0.0～500.0 | 绝对值增加 |

**示例：**
```yaml
Mod属性:
  消耗: 6
  伤害加成: 50.0
  最终伤害: 1.5
  额外范围: 15.0
  技能强度: 1.2
  技能效率: 20.0
  冷却缩减: 10.0
  魔力恢复: 2.5
  魔力上限: 100.0
```

---

### 4. MOD容量计算

#### 4.1 计算公式

```
总消耗 = base-drain + (rank × drain-step)
```

其中：
- **base-drain** = 0级时的基础消耗值
- **rank** = MOD当前等级 (0～10)
- **drain-step** = 每升一级增加的消耗值

#### 4.2 示例计算

假设MOD配置为：
```yaml
Streamline: # 简化 - 技能效率MOD
  base-drain: 4
  drain-step: 1
  max-level: 5
  rarity: "精良"
```

**不同等级的消耗：**
- 等级0: 4 + (0 × 1) = **4点**
- 等级1: 4 + (1 × 1) = **5点**
- 等级2: 4 + (2 × 1) = **6点**
- 等级3: 4 + (3 × 1) = **7点**
- 等级4: 4 + (4 × 1) = **8点**
- 等级5: 4 + (5 × 1) = **9点**

#### 4.3 v1.5.2.4版本变更

**重要：** v1.5.2.4版本已完全**移除极性系统**。
- `polarity` 字段现在**仅作为数据标记**使用
- MOD容量消耗计算**不再受极性影响**
- 极性字段保留在 `mods.yml` 中，但在运行时**被忽略**

**升级影响：**
```kotlin
// v1.5.2.3 (旧版本 - 有极性折扣)
val cost = baseCost * polarityDiscount  // 可能为 2, 4, 6 等

// v1.5.2.4+ (新版本 - 无极性折扣)
val cost = baseCost + (rank * drainStep)  // 直接使用配置值
```

---

## 实际示例

### 示例1：完整技能书NBT

```yaml
# 技能书 - 火球术
类型: "skill_book"
品质: "稀有"

# 基础属性
技能书基础属性:
  技能id: "fireball"
  等级: 5

# 属性强化 (来自装备的MOD)
技能书属性强化:
  技能倍率: 1.5
  最终伤害: 2.0
  伤害加成: 75.0
  额外范围: 15.0
  技能强度: 1.3
  冷却缩减: 15.0
  魔力减耗: 20.0

# MOD装备
Mod系统:
  插槽:
    0:  # MOD #1 - Serration (膛线)
      类型: "mod"
      品质: "精良"
      Mod:
        等级: 8
        id: "serration"
      Mod属性:
        消耗: 12
        伤害加成: 120.0
        
    1:  # MOD #2 - Streamline (简化)
      类型: "mod"
      品质: "精良"
      Mod:
        等级: 3
        id: "streamline"
      Mod属性:
        消耗: 7
        技能效率: 15.0
        
    2: null  # 空插槽
    3: null  # 空插槽
```

### 示例2：独立MOD物品NBT

```yaml
# MOD物品 - 瞬时转场 (复合MOD,具有正负属性)
类型: "mod"
品质: "史诗"

Mod:
  等级: 2
  id: "fleeting_expertise"

Mod属性:
  消耗: 7  # 0级基础(6) + 1*1
  技能效率: 20.0      # 正面属性: 每级 +10% → 2级 = +20%
  冷却缩减: -20.0    # 负面属性: 每级 -10% → 2级 = -20%
```

---

## API使用指南

### 1. ItemStack 扩展函数

#### 读取数据

```kotlin
// 读取字符串
val skillId: String = skillBook.getDeepString(NbtPaths.SkillBook.SKILL_ID, "")

// 读取整数
val level: Int = skillBook.getDeepInt(NbtPaths.SkillBook.LEVEL, 0)

// 读取浮点数
val multiplier: Double = skillBook.getDeepDouble(NbtPaths.SkillBook.MULTIPLIER, 1.0)

// 读取长整数
val timestamp: Long = skillBook.getDeepLong("创建时间", 0L)
```

#### 写入数据

```kotlin
// 写入数据 (会自动清除缓存)
skillBook.setDeep(NbtPaths.SkillBook.LEVEL, 10)
skillBook.setDeep(NbtPaths.SkillBook.MULTIPLIER, 2.5)

// 删除数据
skillBook.removeDeep(NbtPaths.SkillBook.MULTIPLIER)
```

#### 检查数据

```kotlin
// 检查是否有自定义NBT
if (skillBook.hasCustomTag(NbtPaths.SkillBook.SKILL_ID)) {
    println("这是一本技能书!")
}

// 检查特定值
if (skillBook.hasTagValue(NbtPaths.SkillBook.SKILL_ID, "fireball")) {
    println("这是火球术技能书!")
}
```

#### MOD相关快捷函数

```kotlin
// 获取MOD消耗 (默认值为8)
val cost: Int = mod.getModCost()

// 获取MOD等级
val level: Int = mod.getModLevel()

// 获取MOD ID
val modId: String = mod.getModId()

// 获取技能ID (快捷函数)
val skillId: String = skillBook.getSkillId()
```

### 2. ItemTag 扩展函数

```kotlin
// 获取ItemTag缓存 (性能优化)
val tag: ItemTag = item.getCachedTag()

// 直接操作ItemTag读取
val value: Double = tag.getDeepDouble("路径.到.数据", 0.0)

// 累加数值 (用于MOD效果叠加)
tag.addDeep(NbtPaths.SkillBook.DAMAGE_BONUS, 50.0)  // 伤害加成 += 50.0

// 累加整数
tag.addDeepInt(NbtPaths.Mod.LEVEL, 1)  // MOD等级 += 1
```

---

## 常见操作

### 1. 创建一本技能书

```kotlin
val skillBook = ItemStack(Material.PAPER)
skillBook.setDeep(NbtPaths.SkillBook.TYPE, "skill_book")
skillBook.setDeep(NbtPaths.SkillBook.RARITY, "稀有")
skillBook.setDeep(NbtPaths.SkillBook.SKILL_ID, "fireball")
skillBook.setDeep(NbtPaths.SkillBook.LEVEL, 5)

// 设置初始属性强化
skillBook.setDeep(NbtPaths.SkillBook.MULTIPLIER, 1.0)
skillBook.setDeep(NbtPaths.SkillBook.DAMAGE_MORE, 1.0)
skillBook.setDeep(NbtPaths.SkillBook.DAMAGE_BONUS, 0.0)
```

### 2. 在技能书中装备MOD

```kotlin
fun equipModToSkillBook(skillBook: ItemStack, modItem: ItemStack, slotIndex: Int) {
    val modPath = "${NbtPaths.SkillBook.MOD_SLOT_FORMAT}.$slotIndex"
    val modTag = modItem.getCachedTag()
    
    // 复制MOD的NBT数据到插槽
    skillBook.setDeep(modPath, modTag)
    
    // 应用MOD属性到技能书
    val modDamageBonus = modItem.getDeepDouble(NbtPaths.Mod.DAMAGE_BONUS, 0.0)
    val currentBonus = skillBook.getDeepDouble(NbtPaths.SkillBook.DAMAGE_BONUS, 0.0)
    skillBook.setDeep(NbtPaths.SkillBook.DAMAGE_BONUS, currentBonus + modDamageBonus)
}
```

### 3. 计算技能实际伤害

```kotlin
fun calculateSkillDamage(skillBook: ItemStack, basePlayerDamage: Double): Double {
    // 获取所有属性强化
    val multiplier = skillBook.getDeepDouble(NbtPaths.SkillBook.MULTIPLIER, 1.0)
    val damageBonus = skillBook.getDeepDouble(NbtPaths.SkillBook.DAMAGE_BONUS, 0.0)
    val damageMore = skillBook.getDeepDouble(NbtPaths.SkillBook.DAMAGE_MORE, 1.0)
    
    // 伤害计算公式
    var damage = basePlayerDamage
    damage *= (1.0 + damageBonus / 100.0)  // 应用伤害加成 (百分比)
    damage *= multiplier                     // 应用技能倍率
    damage *= damageMore                     // 应用最终伤害 (乘数)
    
    return damage
}
```

### 4. 升级MOD等级

```kotlin
fun upgradeMod(mod: ItemStack) {
    val currentLevel = mod.getModLevel()
    val maxLevel = 10  // 假设最大等级为10
    
    if (currentLevel < maxLevel) {
        val newLevel = currentLevel + 1
        mod.setDeep(NbtPaths.Mod.LEVEL, newLevel)
        
        // 更新容量消耗
        val baseDrain = 4     // 从配置文件获取
        val drainStep = 1     // 从配置文件获取
        val newCost = baseDrain + (newLevel * drainStep)
        mod.setDeep(NbtPaths.Mod.COST, newCost)
    }
}
```

### 5. 检查容量是否超限

```kotlin
fun checkCapacityOverflow(skillBook: ItemStack, maxCapacity: Int = 60): Boolean {
    var usedCapacity = 0
    
    for (i in 0..3) {
        val modPath = "${NbtPaths.SkillBook.MOD_SLOT_FORMAT}.$i"
        val cost = skillBook.getDeepInt("$modPath.${NbtPaths.Mod.COST}", 0)
        usedCapacity += cost
    }
    
    return usedCapacity > maxCapacity
}
```

---

## 数据类型对照表

| 数据类型 | NBT类型 | 范围 | 获取函数 | 示例 |
|--------|--------|------|--------|------|
| String | TAG_String | - | getDeepString() | `"fireball"` |
| Integer | TAG_Int | -2³¹ ~ 2³¹-1 | getDeepInt() | `5` |
| Double | TAG_Double | ±1.7E308 | getDeepDouble() | `1.5` |
| Long | TAG_Long | -2⁶³ ~ 2⁶³-1 | getDeepLong() | `1675000000` |
| CompoundTag | TAG_Compound | - | getDeep() | `{...}` |

---

## 注意事项

### 1. 空物品检查

所有读写函数都会自动检查物品是否为空气:
```kotlin
if (item.isAir()) return default  // 自动返回默认值
```

### 2. 缓存失效

修改NBT后，缓存会自动清除:
```kotlin
item.setDeep(path, value)  // 自动调用 ItemTagCache.invalidate()
```

### 3. 浮点数精度

浮点数可能存在精度问题，避免直接比较:
```kotlin
// 错误 ❌
if (damage == 100.0) { }

// 正确 ✅
if (damage in 99.99..100.01) { }
```

### 4. NBT路径区分大小写

所有NBT路径都区分大小写:
```kotlin
// 错误 ❌
item.getDeepString("技能id")      // 找不到!

// 正确 ✅
item.getDeepString(NbtPaths.SkillBook.SKILL_ID)  // 使用常量
```

---

## 更新历史

### v1.5.3.0 (当前版本)
- 新增MOD配置库扩展到30个
- 发布本文档

### v1.5.2.4
- 完全移除极性系统
- polarity字段现仅作为标记使用

### v1.5.2.0
- 初始NBT结构设计
- 创建NbtPaths常量管理系统

---

## 相关文件

- **NBT常量定义:** `data/NbtPaths.kt`
- **NBT扩展函数:** `util/NbtExtension.kt`
- **常量与配置:** `data/RpgConstants.kt`
- **数据类定义:** `data/DataClasses.kt`
- **MOD配置文件:** `resources/mods.yml`

---

## 许可证

本文档遵循 GNU General Public License v3.0 协议。
