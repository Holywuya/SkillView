# SkillView 文档中心

**SkillView v1.5.3.0 完整文档库**

## 📚 文档导航

### 快速开始

新用户请从这里开始:

1. **[快速参考卡](Quick-Reference-Card.md)** ⭐ *5分钟快速了解*
   - NBT路径速查表
   - MOD配置模板
   - 常用代码片段
   - 30个MOD完整列表

### 详细文档

深入理解系统设计和实现:

2. **[NBT系统详细指南](NBT-System-Guide.md)** 📖 *30分钟精读*
   - 技能书 (SkillBook) NBT完整结构
   - MOD系统 NBT完整结构
   - 实际示例和代码API
   - 常见操作指南
   - 容量计算公式

3. **[MOD配置详细指南](MOD-Configuration-Guide.md)** 🎯 *20分钟参考*
   - MOD配置文件格式说明
   - MOD品质系统详解
   - MOD属性体系 (30+个属性)
   - MOD分类详解 (6大类30个MOD)
   - 配置示例和最佳实践
   - 平衡指导建议

4. **[版本更新日志](../README.md#更新日志)** 📝 *必读变更说明*
   - v1.5.3.0 新增内容
   - v1.5.2.4 极性系统移除
   - 版本兼容性说明

---

## 📊 文档统计

| 文档 | 行数 | 大小 | 内容焦点 |
|------|------|------|--------|
| Quick-Reference-Card | 426 | 12K | 速查参考 |
| NBT-System-Guide | 587 | 16K | 数据结构 |
| MOD-Configuration-Guide | 491 | 16K | 配置和平衡 |
| **总计** | **1,672** | **56K** | - |

---

## 🎯 按需求查找文档

### 我想要...

#### ❓ 快速上手
→ 阅读 [快速参考卡](Quick-Reference-Card.md)

#### 💡 理解NBT数据结构
→ 阅读 [NBT系统详细指南](NBT-System-Guide.md)

#### ⚙️ 配置或修改MOD
→ 阅读 [MOD配置详细指南](MOD-Configuration-Guide.md)

#### 🔍 查找特定NBT路径
→ 参考 `Quick-Reference-Card.md` 的 *NBT路径速查表*

#### 📋 查看所有30个MOD
→ 参考 `Quick-Reference-Card.md` 的 *30个MOD一览表*

#### 🛠️ 编写代码操作NBT
→ 参考 `NBT-System-Guide.md` 的 *API使用指南* 部分

#### 💰 计算MOD容量消耗
→ 参考 `NBT-System-Guide.md` 的 *MOD容量计算* 或 `Quick-Reference-Card.md` 的 *容量计算速查*

#### 🎨 设计新MOD
→ 参考 `MOD-Configuration-Guide.md` 的 *配置示例与最佳实践*

#### 🔗 理解版本变更
→ 阅读 [README](../README.md#更新日志) 的 *更新日志* 部分

---

## 📖 阅读路径推荐

### 对于游戏玩家

```
快速参考卡 (了解30个MOD)
    ↓
NBT系统详细指南 (了解数据结构)
    ↓
MOD配置详细指南 (了解MOD选择)
```

### 对于插件开发者

```
NBT系统详细指南 (完整学习)
    ↓
快速参考卡 (查找代码片段)
    ↓
MOD配置详细指南 (理解平衡)
```

### 对于MOD配置师

```
MOD配置详细指南 (重点章节)
    ↓
快速参考卡 (模板参考)
    ↓
NBT系统详细指南 (深入理解)
```

---

## 🔧 主要特性

### 技能书 (SkillBook)
- ✅ 基础信息 (类型、品质)
- ✅ 基础属性 (技能ID、等级)
- ✅ 属性强化 (7种MOD属性加成)
- ✅ MOD系统 (4个插槽装备系统)

### MOD系统 (Mod)
- ✅ 30个预设MOD配置
- ✅ 6大分类 (攻击/防御/生存/技能/工具/特殊)
- ✅ 品质系统 (普通/精良/稀有/史诗)
- ✅ 9项核心属性支持 (可扩展)
- ✅ 容量管理系统
- ✅ 堕落MOD设计支持

---

## 📌 关键概念速查

### 容量计算公式

```
总消耗 = base-drain + (当前等级 × drain-step)
```

### 伤害计算公式

```
最终伤害 = 基础伤害
         × (1 + 伤害加成% / 100)
         × 技能倍率
         × 最终伤害倍数
         × (1 + 技能强度% / 100)
```

### MOD品质系统

- **普通**: 低消耗，适合基础属性，max-level=10
- **精良**: 中等消耗，适合单一强力属性，max-level=10
- **稀有**: 高消耗，适合多属性/复合效果，max-level=5
- **史诗**: 最高消耗，适合顶级效果/堕落MOD，max-level=3

---

## 🎯 30个MOD完整列表

### 攻击类 (7款)
1. Serration - 膛线
2. Hellfire - 地狱火焰
3. Frostbite - 寒冰之刃
4. SplitChamber - 分裂弹头
5. CriticalChance - 精准打击
6. CriticalDamage - 致命一击
7. StatusChance - 诅咒符文

### 防御类 (5款)
8. SteelSkin - 钢铁皮肤
9. Redirection - 方向盾牌
10. FlameResistance - 炎热防护
11. FrostResistance - 极寒防护
12. LightningResistance - 绝缘防护

### 生存类 (4款)
13. Vitality - 生命力
14. Flow - 川流不息
15. LifeLeech - 生命汲取
16. EnergyRegeneration - 能量流动

### 技能类 (7款)
17. Streamline - 简化
18. Stretch - 延伸
19. Continuity - 持久化
20. Intensify - 强化
21. FleetingExpertise - 瞬时转场
22. Overextend - 越界
23. CorruptedBlood - 腐化之血

### 工具类 (3款)
24. Rush - 急速
25. Haste - 迅捷
26. Vigilance - 警惕

### 特殊类 (4款)
27. Reflexes - 反射神经
28. Berserk - 狂暴
29. Sacrifice - 献祭
30. Sanctuary - 圣殿

---

## 🔗 相关文件

| 文件 | 位置 | 描述 |
|------|------|------|
| mods.yml | `src/main/resources/` | MOD配置定义 |
| NbtPaths.kt | `src/main/kotlin/com/skillview/data/` | NBT常量 |
| NbtExtension.kt | `src/main/kotlin/com/skillview/util/` | NBT操作API |
| RpgConstants.kt | `src/main/kotlin/com/skillview/data/` | 游戏常量 |
| DataClasses.kt | `src/main/kotlin/com/skillview/data/` | 数据类定义 |

---

## 📋 技术栈

- **TabooLib 6.x** - 插件框架
- **Bukkit/Spigot** - 服务器API
- **Kotlin** - 编程语言
- **MythicMobs** - 技能触发 (可选)
- **AttributeCore** - 属性系统 (可选)

---

## 📝 版本信息

- **当前版本:** v1.5.3.0
- **发布日期:** 2026-02-01
- **文档更新:** 2026-02-01

### 版本特性

✨ **v1.5.3.0**
- 🎉 MOD配置库扩展到30个
- 📚 发布完整三部曲文档
- 🚀 新增8个MOD分类组织

⚙️ **v1.5.2.4**
- 🔧 极性系统完全移除
- ✅ 容量计算简化

---

## 📖 推荐阅读顺序

### 第一次接触SkillView

1. [快速参考卡](Quick-Reference-Card.md) (15分钟)
   - 了解基本概念
   - 查看MOD列表
   - 学习常用代码

2. [NBT系统详细指南](NBT-System-Guide.md) (30分钟)
   - 深入理解数据结构
   - 学习API使用
   - 掌握计算公式

3. [MOD配置详细指南](MOD-Configuration-Guide.md) (20分钟)
   - 理解配置格式
   - 学习MOD设计
   - 参考最佳实践

**总耗时:** ~65分钟 ✅

---

## 💡 常见问题

**Q: 我是玩家，应该读哪个文档?**
A: 快速参考卡 + NBT系统详细指南的前半部分

**Q: 我是开发者，应该读哪个文档?**
A: NBT系统详细指南 + 快速参考卡的代码片段

**Q: 我想添加新MOD，应该读哪个文档?**
A: MOD配置详细指南 + 快速参考卡的模板

**Q: 怎样快速找到某个NBT路径?**
A: 快速参考卡 → NBT路径速查表

**Q: 如何计算MOD容量?**
A: 快速参考卡 → 容量计算速查

---

## 🔍 全文搜索

使用您的编辑器的搜索功能 (Ctrl+F) 查找:
- 特定MOD名称
- NBT路径
- 函数名
- 配置参数

---

## 📞 获取帮助

- **GitHub Issues:** https://github.com/Holywuya/SkillView/issues
- **代码仓库:** https://github.com/Holywuya/SkillView
- **社区文档:** https://taboolib.maplex.top/

---

## 📄 许可证

所有文档遵循 **GNU General Public License v3.0** 协议。

---

## 更新历史

### 2026-02-01
- 📚 发布完整文档三部曲
- 🎉 1500+行文档内容
- 📊 30个MOD详细说明

### 2026-01-31
- 📝 文档中心创建

---

**最后更新:** 2026-02-01  
**下次更新预计:** v1.6.0 发布时  
**文档维护者:** SkillView 开发团队
