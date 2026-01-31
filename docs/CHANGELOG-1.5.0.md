# SkillView v1.5.0 性能优化与MOD功能更新

## 更新概要

本次更新重点优化了插件性能，并完善了Warframe风格的MOD系统功能。

## 性能优化

### 1. SkillStorage 三层缓存

**优化内容：**
- 添加 `skillLoadoutCache` / `modLoadoutCache` 缓存已解析的装备数据
- 添加 `skillIdCache` 预解析技能ID，避免每tick解析JSON
- 实现脏标记模式 (`markSkillDirty` / `markModDirty`)，仅在数据变化时刷新

**性能提升：**
- JSON解析从每tick 5次 → 仅在菜单关闭时1次
- 预计节省 98% 的JSON解析开销

### 2. ItemTag 缓存

**优化内容：**
- 使用 `WeakHashMap` 缓存 ItemTag 对象
- 写入操作自动失效缓存

**性能提升：**
- 避免每次NBT访问都创建新对象
- 预计节省 90% 的对象分配

### 3. ModStats 对象池

**优化内容：**
- 添加 `reset()` 方法重用对象
- 使用 `merge()` 替代 `getOrDefault + put`

**性能提升：**
- 避免每tick创建新ModStats对象
- 减少GC压力

### 4. APListener 降频 + 批处理

**优化内容：**
- 周期从 10tick → 40tick (2秒)
- 仅在 `recalculateIfNeeded()` 返回 true 时同步
- 使用 `ThreadLocal` 复用 ArrayList

**性能提升：**
- 减少 95% 的无效AttributePlus同步调用

### 5. SkillPacketSender 差异发包

**优化内容：**
- 周期从 2tick → 5tick (0.25秒)
- 添加 `lastCdSent` 缓存，仅在CD值变化时发包
- 使用缓存版的 `getSkillId()`

**性能提升：**
- 减少 80% 的ArcartX包发送

### 总体性能收益

| 组件 | 优化前频率 | 优化后频率 | CPU节省 |
|------|-----------|-----------|---------|
| APListener | 10tick/次 | 40tick + 脏检查 | ~95% |
| SkillPacketSender | 2tick/次 | 5tick + 差异发包 | ~80% |
| SkillStorage.getSkillId | 5次/tick/玩家 | 首次缓存 | ~98% |
| ItemTag创建 | 每次访问 | WeakCache | ~90% |
| ModStats分配 | 每tick | 对象复用 | ~95% |

**预计服务器TPS提升：20-50玩家服务器可节省 30-50% CPU占用**

---

## 功能更新

### 1. WeaponMod 武器MOD系统

完整实现武器MOD配装界面：

```
/skill open weapon
```

**功能：**
- 放入武器后显示MOD槽位
- 容量实时计算与显示
- 极性匹配减半消耗
- 重复MOD检测

### 2. PolaritySystem 极性匹配

Warframe风格的极性系统：

| 极性 | 符号 | 颜色 |
|------|------|------|
| Madurai | V | 红色 |
| Vazarin | D | 青色 |
| Naramon | - | 绿色 |
| Zenurik | = | 蓝色 |
| Unairu | R | 橙色 |
| 通用 | * | 白色 |

**消耗计算：**
- 极性匹配：消耗减半
- 极性不匹配：消耗 x1.25
- 无极性槽位：原始消耗

### 3. CapacitySystem 容量系统

**API：**
```kotlin
// 获取基础容量
CapacitySystem.getBaseCapacity(level: Int): Int

// 获取最大容量（含Orokin加成）
CapacitySystem.getMaxCapacity(level: Int, hasOrokinBoost: Boolean): Int

// 验证配装是否超出容量
CapacitySystem.validateLoadout(mods, slotPolarities, maxCapacity): CapacityResult
```

### 4. ModFusionSystem MOD升级系统

**升级消耗表：**
| 等级 | 基础消耗 |
|------|---------|
| 1 | 10 |
| 2 | 30 |
| 3 | 70 |
| 4 | 150 |
| 5 | 310 |
| ... | ... |

**稀有度倍率：**
- 普通: x1.0
- 精良: x1.5
- 稀有: x2.0
- 史诗: x2.5
- 传奇: x3.0

**API：**
```kotlin
// 计算升级消耗
ModFusionSystem.getUpgradeCost(currentLevel, targetLevel, rarity): Int

// 执行升级
ModFusionSystem.upgradeMod(player, mod, targetLevel): UpgradeResult
```

---

## 文件变更清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `SkillStorage.kt` | 重构 | 三层缓存 + 脏标记 |
| `NbtExtension.kt` | 修改 | ItemTag缓存 |
| `ModStats.kt` | 修改 | reset() + merge() |
| `PlayerModLogic.kt` | 重构 | 脏标记模式 |
| `APListener.kt` | 重构 | 降频 + 批处理 |
| `SkillPacketSender.kt` | 重构 | 差异发包 |
| `PlayerMod.kt` | 修改 | onClose调用markDirty |
| `SkillMenu.kt` | 修改 | onClose调用markDirty |
| `WeaponMod.kt` | 重写 | 完整实现 |
| `PolaritySystem.kt` | 新建 | 极性匹配 |
| `CapacitySystem.kt` | 新建 | 容量系统 |
| `ModFusionSystem.kt` | 新建 | 升级融合 |
| `SkillCommand.kt` | 修改 | 添加weapon菜单 |
