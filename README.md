# SkillView

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Holywuya/SkillView)

**版本: 1.5.3.0**

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

### v1.5.3.0

**极性槽系统实现:**
- 为所有MOD菜单添加极性槽位配置UI，允许玩家为每个MOD槽配置极性
- **武器MOD**: 新增4个极性槽配置按钮（第5行），点击打开极性选择菜单
- **角色MOD**: 新增4个极性槽配置按钮（第5行），点击打开极性选择菜单
- **技能MOD**: 新增4个极性槽配置按钮（第5行），点击打开极性选择菜单

**极性选择菜单:**
- 创建可复用的 PolaritySelectionMenu，所有MOD菜单共用
- 支持8种极性类型选择：V(红)、D(蓝)、-(绿)、=(蓝紫)、R(橙)、Y(粉)、*(通用)、无
- 每个极性按钮显示当前极性状态和选择提示
- 点击选择后自动关闭菜单并保存配置

**数据结构更新:**
- SkillStorage.ModLoadout 新增 slotPolarities 字段，存储各槽位极性
- WeaponModLoadout 已有此字段，现已启用
- SkillMod 极性存储在技能书NBT中："技能MOD.槽位X.极性"

**工作流改进:**
- 所有MOD菜单在打开时自动刷新极性显示
- 极性配置立即保存到玩家数据或技能书NBT
- 极性槽位超容拦截已支持（容量计算传入slotPolarities）

**新增文件:**
- SlotPolarityManager.kt - 极性槽管理核心（预留，未在菜单中使用）
- PolaritySelectionMenu.kt - 可复用的极性选择菜单

**修改文件:**
- WeaponMod.kt - 新增6行UI，添加4个极性配置按钮和极性显示函数
- PlayerMod.kt - 新增5行UI，添加4个极性配置按钮和极性显示函数
- SkillMod.kt - 新增5行UI，添加4个极性配置按钮和极性显示函数
- SkillStorage.kt - ModLoadout 数据类新增 slotPolarities 字段

**后续计划:**
- 极性槽配置持久化（已支持，需测试）
- 极性匹配容量折扣应用在菜单中调用 calculateUsedCapacity()
- 完整集成测试

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
