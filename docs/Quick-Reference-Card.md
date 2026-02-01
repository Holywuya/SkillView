# SkillView 快速参考卡

**版本:** v1.5.3.0 | **最后更新:** 2026-02-01

---

## 快速索引

| 主题 | 文档位置 |
|------|--------|
| NBT数据结构 | `docs/NBT-System-Guide.md` |
| MOD配置详解 | `docs/MOD-Configuration-Guide.md` |
| MOD列表 | `resources/mods.yml` |
| 代码常量 | `data/NbtPaths.kt` |

---

## NBT路径速查表

### 技能书 (SkillBook)

```
类型                                    → 技能书类型标识
品质                                    → 品质等级 (普通/精良/稀有/史诗)

技能书基础属性.技能id                    → 技能唯一ID
技能书基础属性.等级                      → 技能等级 (0-10)

技能书属性强化.技能倍率                  → 技能伤害倍率
技能书属性强化.最终伤害                  → 最终伤害乘数
技能书属性强化.伤害加成                  → 伤害百分比加成
技能书属性强化.额外范围                  → 技能范围增加
技能书属性强化.技能强度                  → 技能效果强度
技能书属性强化.冷却缩减                  → 技能冷却减少
技能书属性强化.魔力减耗                  → 魔力消耗减少

Mod系统.插槽.0-3                        → 4个MOD装备插槽
```

### MOD (Mod)

```
类型                                    → MOD类型标识
品质                                    → MOD品质等级

Mod.等级                                → 当前强化等级
Mod.id                                  → MOD唯一ID

Mod属性.消耗                            → 容量消耗值
Mod属性.伤害加成                        → 伤害百分比加成
Mod属性.最终伤害                        → 最终伤害乘数
Mod属性.额外范围                        → 范围增加量
Mod属性.技能强度                        → 技能效果倍率
Mod属性.技能效率                        → 魔力消耗减少
Mod属性.冷却缩减                        → 冷却时间减少
Mod属性.魔力恢复                        → 每秒魔力回复
Mod属性.魔力上限                        → 最大魔力增加
// 注意: 以下属性需在NbtPaths.kt中定义后才能使用:
// Mod属性.护甲、Mod属性.生命、Mod属性.元素伤害等
// 目前代码仅支持上方列出的9个属性
```

---

## MOD品质快速参考

| 品质 | 代码 | 容量系数 | 升级成本 | 推荐等级上限 |
|------|------|--------|--------|-----------|
| 普通 | `普通` | 1.0x | 低 | 10 |
| 精良 | `精良` | 1.2x | 中 | 10 |
| 稀有 | `稀有` | 1.5x | 较高 | 5 |
| 史诗 | `史诗` | 2.0x | 高 | 3 |

---

## MOD配置模板

### 基础MOD (单属性)

```yaml
MOD_NAME:
  display: "&X&lMOD名称"
  description: "功能描述"
  material: "PAPER"
  rarity: "普通"
  polarity: "V"
  base-drain: 2
  drain-step: 1
  attributes:
    "属性名": 数值
  max-level: 10
  tags: ["分类"]
```

### 复合MOD (多属性)

```yaml
MOD_NAME:
  display: "&X&lMOD名称"
  description: "功能描述"
  material: "PAPER"
  rarity: "稀有"
  polarity: "="
  base-drain: 6
  drain-step: 1
  attributes:
    "属性1": 数值1
    "属性2": 数值2
  max-level: 5
  tags: ["分类", "复合"]
```

### 堕落MOD (正负属性)

```yaml
MOD_NAME:
  display: "&X&lMOD名称"
  description: "功能描述"
  material: "PAPER"
  rarity: "史诗"
  polarity: "-"
  base-drain: 8
  drain-step: 1
  attributes:
    "正面属性": 数值
    "负面属性": -数值
  max-level: 3
  tags: ["分类", "堕落"]
```

---

## 容量计算速查

### 容量消耗公式

```
总消耗 = base-drain + (等级 × drain-step)
```

### 常见消耗示例

| MOD名称 | base-drain | drain-step | 等级1 | 等级5 | 等级10 |
|--------|-----------|-----------|------|------|--------|
| Serration | 4 | 1 | 5 | 9 | 14 |
| Streamline | 4 | 1 | 5 | 9 | 14 |
| Stretch | 2 | 1 | 3 | 7 | 12 |
| FleetingExpertise | 6 | 1 | 7 | 11 | - |
| Overextend | 8 | 1 | 9 | 13 | - |

---

## 代码快速参考

### 读取NBT数据

```kotlin
// 字符串
val value = item.getDeepString(NbtPaths.SkillBook.SKILL_ID, "default")

// 整数
val level = item.getDeepInt(NbtPaths.SkillBook.LEVEL, 0)

// 浮点数
val multiplier = item.getDeepDouble(NbtPaths.SkillBook.MULTIPLIER, 1.0)

// 长整数
val timestamp = item.getDeepLong("timestamp", 0L)
```

### 写入NBT数据

```kotlin
item.setDeep(NbtPaths.SkillBook.LEVEL, 10)
item.setDeep(NbtPaths.SkillBook.MULTIPLIER, 2.5)
```

### MOD快捷函数

```kotlin
val cost = mod.getModCost()          // 获取容量消耗
val level = mod.getModLevel()        // 获取MOD等级
val modId = mod.getModId()           // 获取MOD ID
val skillId = item.getSkillId()      // 获取技能ID
```

### 检查和操作

```kotlin
// 检查是否有自定义NBT
if (item.hasCustomTag(NbtPaths.SkillBook.SKILL_ID)) { }

// 检查特定值
if (item.hasTagValue(NbtPaths.SkillBook.SKILL_ID, "fireball")) { }

// 删除数据
item.removeDeep(NbtPaths.SkillBook.MULTIPLIER)

// 累加数值
tag.addDeep(NbtPaths.SkillBook.DAMAGE_BONUS, 50.0)
```

---

## ChatColor代码速查

| 代码 | 颜色 | 代码 | 颜色 |
|------|------|------|------|
| `&0` | 黑色 | `&8` | 暗灰 |
| `&1` | 深蓝 | `&9` | 蓝色 |
| `&2` | 深绿 | `&a` | 绿色 |
| `&3` | 深青 | `&b` | 浅青 |
| `&4` | 深红 | `&c` | 红色 |
| `&5` | 紫色 | `&d` | 粉红 |
| `&6` | 金色 | `&e` | 黄色 |
| `&7` | 浅灰 | `&f` | 白色 |

**样式:**
- `&l` = 加粗
- `&o` = 斜体
- `&n` = 下划线
- `&m` = 删除线

---

## Bukkit Material 常用参考

```
PAPER          → 纸张 (默认物品)
BOOK           → 书籍
DIAMOND        → 钻石
EMERALD        → 翡翠
AMETHYST_SHARD → 紫水晶碎片
ECHO_SHARD     → 回响碎片
```

---

## 30个MOD一览表

| # | MOD ID | 名称 | 品质 | 分类 |
|----|--------|------|------|------|
| 1 | Serration | 膛线 | 精良 | 攻击 |
| 2 | Hellfire | 地狱火焰 | 稀有 | 攻击 |
| 3 | Frostbite | 寒冰之刃 | 稀有 | 攻击 |
| 4 | SplitChamber | 分裂弹头 | 史诗 | 攻击 |
| 5 | CriticalChance | 精准打击 | 精良 | 攻击 |
| 6 | CriticalDamage | 致命一击 | 精良 | 攻击 |
| 7 | StatusChance | 诅咒符文 | 精良 | 攻击 |
| 8 | SteelSkin | 钢铁皮肤 | 精良 | 防御 |
| 9 | Redirection | 方向盾牌 | 普通 | 防御 |
| 10 | FlameResistance | 炎热防护 | 普通 | 防御 |
| 11 | FrostResistance | 极寒防护 | 普通 | 防御 |
| 12 | LightningResistance | 绝缘防护 | 普通 | 防御 |
| 13 | Vitality | 生命力 | 普通 | 生存 |
| 14 | Flow | 川流不息 | 普通 | 生存 |
| 15 | LifeLeech | 生命汲取 | 稀有 | 生存 |
| 16 | EnergyRegeneration | 能量流动 | 精良 | 生存 |
| 17 | Streamline | 简化 | 精良 | 技能 |
| 18 | Stretch | 延伸 | 普通 | 技能 |
| 19 | Continuity | 持久化 | 普通 | 技能 |
| 20 | Intensify | 强化 | 精良 | 技能 |
| 21 | FleetingExpertise | 瞬时转场 | 史诗 | 技能 |
| 22 | Overextend | 越界 | 史诗 | 技能 |
| 23 | CorruptedBlood | 腐化之血 | 史诗 | 技能 |
| 24 | Rush | 急速 | 普通 | 工具 |
| 25 | Haste | 迅捷 | 普通 | 工具 |
| 26 | Vigilance | 警惕 | 普通 | 工具 |
| 27 | Reflexes | 反射神经 | 稀有 | 特殊 |
| 28 | Berserk | 狂暴 | 史诗 | 特殊 |
| 29 | Sacrifice | 献祭 | 史诗 | 特殊 |
| 30 | Sanctuary | 圣殿 | 稀有 | 特殊 |

---

## 常见错误排查

### NBT相关

| 问题 | 原因 | 解决方案 |
|------|------|--------|
| 读取值为0或默认值 | NBT键不存在或为空 | 检查键名大小写,使用NbtPaths常量 |
| 修改后值未生效 | 缓存未更新 | 使用setDeep()会自动清除缓存 |
| 读取精度不对 | 浮点数精度问题 | 使用范围比较而非直接相等 |

### MOD相关

| 问题 | 原因 | 解决方案 |
|------|------|--------|
| MOD不显示 | 配置文件格式错误 | 检查YAML缩进和键名 |
| 容量计算错误 | base-drain或drain-step配置错 | 验证公式:base + (level × step) |
| 属性未应用 | 属性名不匹配 | 确认属性名在mods.yml中定义 |

---

## 性能优化提示

1. **使用缓存:** `getDeepString()` 内部已使用 WeakHashMap 缓存
2. **批量操作:** 避免频繁调用 setDeep(),合并修改后一次性保存
3. **异步操作:** 访问NBT较慢,考虑异步处理大量物品
4. **MOD计算:** 容量计算结果可缓存,避免每次都重新计算

---

## 版本兼容性

### v1.5.3.0 (当前)
- ✅ 30个MOD可用
- ✅ 所有属性正常工作
- ✅ NBT系统稳定

### v1.5.2.4
- ✅ 极性系统已移除
- ✅ polarity字段纯标记
- ✅ 容量计算简化

### 升级建议
- 存档自动兼容
- 无需特殊迁移
- 旧MOD配置继续有效

---

## 有用的命令

```bash
# 给予技能书
/skill give book <player> <skillId>

# 给予星愿点数 (用于升级)
/skill give points <player> <amount>

# 打开技能装备栏
/skill open equip

# 打开角色MOD配装
/skill open player

# 打开技能MOD配装
/skill open skill

# 打开武器MOD配装
/skill open weapon

# 释放技能 (槽位0-4)
/skill cast <slot>
```

---

## 常用Kotlin代码片段

### 创建技能书

```kotlin
val skillBook = ItemStack(Material.PAPER)
skillBook.setDeep(NbtPaths.SkillBook.TYPE, "skill_book")
skillBook.setDeep(NbtPaths.SkillBook.RARITY, "稀有")
skillBook.setDeep(NbtPaths.SkillBook.SKILL_ID, "fireball")
skillBook.setDeep(NbtPaths.SkillBook.LEVEL, 1)
```

### 装备MOD

```kotlin
val slotPath = "${NbtPaths.SkillBook.MOD_SLOT_FORMAT}.0"
skillBook.setDeep(slotPath, mod.getCachedTag())
```

### 计算总容量

```kotlin
var totalCapacity = 0
for (i in 0..3) {
    totalCapacity += skillBook.getDeepInt(
        "${NbtPaths.SkillBook.MOD_SLOT_FORMAT}.$i.${NbtPaths.Mod.COST}",
        0
    )
}
```

### 升级MOD等级

```kotlin
val currentLevel = mod.getModLevel()
if (currentLevel < 10) {
    mod.setDeep(NbtPaths.Mod.LEVEL, currentLevel + 1)
}
```

---

## 相关资源

| 文件 | 位置 | 用途 |
|------|------|------|
| mods.yml | `resources/` | MOD配置 |
| NbtPaths.kt | `data/` | NBT常量定义 |
| NbtExtension.kt | `util/` | NBT操作函数 |
| NBT-System-Guide.md | `docs/` | 详细文档 |
| MOD-Configuration-Guide.md | `docs/` | 配置指南 |

---

## 获取帮助

- **GitHub Issues:** https://github.com/Holywuya/SkillView/issues
- **社区Wiki:** https://taboolib.maplex.top/docs/intro/
- **代码注释:** 查看源文件中的详细注释

---

**最后更新:** 2026-02-01  
**SkillView 版本:** v1.5.3.0  
**许可证:** GNU General Public License v3.0
