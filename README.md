# SkillView

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Holywuya/SkillView)

**版本: 1.5.0.0**

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
- **AttributePlus** (属性系统)
- **MythicMobs** 5.x (技能释放)
- **ArcartX** (UI渲染)
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
