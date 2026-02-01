# SkillView

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Holywuya/SkillView)

**版本: 1.5.2.3**

Warframe风格的技能与MOD系统插件，基于 TabooLib 6.x 开发。

## 功能特性

- **技能系统**: 技能书装备、技能释放、冷却管理
- **角色MOD**: 全局属性加成（生命力、暴击、技能效率等）
- **技能MOD**: 单技能属性强化（伤害加成、范围增加等）
- **武器MOD**: 武器专属MOD配装
- **极性系统**: Warframe风格的极性匹配，减少容量消耗
- **容量系统**: MOD消耗限制与管理
- **MOD升级**: 使用星愿点数强化MOD等级

## 依赖插件

- **TabooLib** 6.2.4+
- **MythicMobs** 5.x (技能释放)
- **ArcartX** (UI渲染)
- **AttributeCore** (可选, 属性系统 - 用于技能伤害计算)
- **Vault** (可选, 经济系统)

## 命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/skill open equip` | skill.use | 打开技能装备栏 |
| `/skill open player` | skill.use | 打开角色MOD配装 |
| `/skill open skill` | skill.use | 打开技能MOD配装 |
| `/skill open weapon` | skill.use | 打开武器MOD配装 |
| `/skill cast <0-4>` | skill.use | 释放指定槽位技能 |
| `/skill get <skillId>` | skill.admin | 获取技能书 |
| `/skill give points <player> <amount>` | skill.admin | 给予星愿点数 |
| `/skill give book <player> <skillId>` | skill.admin | 给予技能书 |

## 构建

### 发行版本

```bash
./gradlew build
```

### 开发版本

```bash
./gradlew taboolibBuildApi -PDeleteCode
```

## 更新日志

### v1.5.2.3

**配置与常量优化 (代码规范化):**
- 统一所有 NBT 路径定义到 `NbtPaths.kt`（18+ 处硬编码字符串）
- 统一所有配置常量到 `RpgConstants.kt`（GameConfig, StorageKeys, ModTypes, Rarities）
- 创建 `RpgDefinitions.kt` 作为公共 API，简化常量访问
- **优化效果：** 单一真实来源 (SSOT) 原则，减少维护成本，提升代码质量

**修改文件 (7个):**
- NbtPaths.kt - 添加 MOD_SLOT_FORMAT、文档完善
- RpgConstants.kt - 添加 4 个新的常量对象
- RpgDefinitions.kt - 添加常量 API 代理
- NbtExtension.kt - 使用统一 NBT 常量
- SkillStorage.kt - 使用统一存储键常量
- SkillModLogic.kt - 使用 MOD_SLOT_FORMAT
- SkillCaster.kt - 使用统一技能书 NBT 常量

**向后兼容性:** 完全兼容，无 breaking changes

### v1.5.2.2

**容量系统强化:**
- **武器MOD**: 添加容量超限拦截，超出容量时阻止装备并显示详细错误信息
- **角色MOD**: 添加完整的容量显示和验证系统（之前完全缺失）
- **技能MOD**: 添加完整的容量显示和验证系统（之前完全缺失）
- 所有MOD配装界面现在显示：已用容量/总容量，极性匹配提示
- 超出容量时显示："&c容量不足！需要: &eXX&c/&f60 &7(超出 &cX&7)"

**修改文件:**
- WeaponMod.kt - 在 conditionSlot 中添加容量检查逻辑
- PlayerMod.kt - 添加容量显示、更新逻辑和装备验证
- SkillMod.kt - 添加容量显示、布局调整和装备验证

### v1.5.2.1

**Bug 修复:**
- 修复 AttributeCore API 调用错误：将属性键从 `"attack_damage"` (占位符) 改为 `"攻击力"` (实际属性名)
- 该修复确保技能伤害计算能正确读取玩家的攻击力属性值

**技术细节:**
- AttributeCore 内部使用中文属性名作为键 (如 `"攻击力"`)
- `"attack_damage"` 仅用于 PlaceholderAPI 占位符
- 更新后的 API 调用：`AttributeCoreAPI.getAttribute(player, "攻击力")`

### v1.5.2.0

**AttributeCore 集成:**
- 集成 AttributeCore 属性系统作为可选依赖
- 技能伤害计算现使用 AttributeCore 的 `攻击力` 属性
- 如果未安装 AttributeCore，自动使用硬编码默认值 100.0
- 移除了 config.yml 中的 BasePlayerDamage 配置项

**新增文件:**
- AttributeCoreExtension.kt - AttributeCore API 包装器

**配置变更:**
- 移除 config.yml 中的 BasePlayerDamage 配置项
- 现在从 AttributeCore 动态获取攻击力属性

**技术细节:**
- 使用 `AttributeCoreAPI.getAttribute(player, "攻击力")` 获取攻击力
- 插件检测机制：运行时检测 AttributeCore 是否安装
- 安全降级：未安装时自动使用默认值，不影响插件运行

### v1.5.1.0

**破坏性变更:**
- 移除 AttributePlus 依赖
- 技能伤害计算改用配置文件中的 BasePlayerDamage 参数

**删除的文件:**
- AttributePlusExtension.kt
- AttributePlusMM.kt
- APListener.kt
- libs/AttributePlus-API.jar

**配置变更:**
- config.yml 新增 BasePlayerDamage 配置项

### v1.5.0.0

**性能优化:**
- SkillStorage 三层缓存 + 脏标记模式
- ItemTag WeakHashMap 缓存
- ModStats 对象池优化
- APListener 降频至2秒 + 批处理
- SkillPacketSender 差异发包机制

**功能更新:**
- 完整的武器MOD配装系统
- 极性匹配系统 (PolaritySystem)
- 容量计算系统 (CapacitySystem)
- MOD升级融合系统 (ModFusionSystem)

详细更新请查看 [CHANGELOG-1.5.0.md](docs/CHANGELOG-1.5.0.md)

## 开源协议

GNU General Public License v3.0
