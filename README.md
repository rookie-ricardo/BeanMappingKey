<h2 align="center">BeanMappingKey</h2>

<p align="center">
  <a title="GitHub Stars" target="_blank" href="https://github.com/rookie-ricardo/BeanMappingKey/stargazers"><img alt="GitHub Stars" src="https://img.shields.io/github/stars/rookie-ricardo/BeanMappingKey.svg?label=Stars&style=social"></a>  
  <a title="GitHub Forks" target="_blank" href="https://github.com/rookie-ricardo/BeanMappingKey/network/members"><img alt="GitHub Forks" src="https://img.shields.io/github/forks/rookie-ricardo/BeanMappingKey.svg?label=Forks&style=social"></a>
  <a title="GitHub Watchers" target="_blank" href="https://github.com/rookie-ricardo/BeanMappingKey/watchers"><img alt="GitHub Watchers" src="https://img.shields.io/github/watchers/rookie-ricardo/BeanMappingKey.svg?label=Watchers&style=social"></a>
  <br>
  <br>
  <a title="简体中文" href="#">简体中文</a> | <a title="English" href="README_EN.md">English</a>
</p>

<p align="center">  
  一键生成两个实体类之间的字段映射代码，用于代替 BeanUtil 与 MapStruct 等工具。
  <br>
</p>

<br/>

## 安装指南

- 在 `Windows` 系统上安装:
    - <kbd>File</kbd> > <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>Browse repositories...</kbd> > <kbd>Search for "BeanMappingKey"</kbd> > <kbd>Install Plugin</kbd>
- 在 `MacOS` 系统上安装:
    - <kbd>Preferences</kbd> > <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>Browse repositories...</kbd> > <kbd>Search for "BeanMappingKey"</kbd> > <kbd>Install Plugin</kbd>
- 手动安装:
    - 下载 [latest release](https://github.com/rookie-ricardo/BeanMappingKey/releases/latest) 之后， 选择 <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Install plugin from disk...</kbd>

注：暂且只支持 2020 以上版本的 IDEA，安装之后无需重启。

<br/>

## 使用指南

在 Java 开发的过程中，经常会使用众多包装型的对象如：BO、VO、DTO，它们之间往往只有两三个字段的差异， 
而对它们进行相互转换则是一项耗时耗力的体力活，所以 Java 生态中就衍生出了 `BeanUtil` 与 `MapStruct` 这种工具，
然而 `BeanUtil` 无法查看对象转换细节，`MapStruct` 有时则会遇到对象转换不尽如人意的情况，此时你仍需手动编写对象转换代码。

<br/>

BeanMappingKey 就是为了解决繁琐的手动对象转换而开发的，它（暂时）一共有三种用法：

- 选中一个带有返回值和参数的方法，使用快捷键 `Ctrl+M` 或者右键点击 BeanMappingKey，
就能自动插入相关转换代码。
- 选中一个变量，使用快捷键 `Ctrl+M` 或者右键点击 BeanMappingKey，就能自动生成转换代码到粘贴板。
- 选中一个类，使用快捷键 `Ctrl+M` 或者右键点击 BeanMappingKey，就能自动生成转换代码到粘贴板。

以上生成逻辑中，若有 `Builder` 内部类则优先生成 `Builder` 代码，其次生成 `Set` 型代码。

Tip: 在最新的 2.X 版本中，它连嵌套对象也可以生成了，比如一个 `User` 对象中包含了一个 `Address` 对象，
那么它将会将这两个对象都生成出来，并且自动赋值。

<br/>

## 代码生成指南

仔细阅读代码生成指南可以帮助你理解本插件生成代码的逻辑，倘若你在使用中遇到问题可以更好的判断是插件问题还是用法问题。


**Q1: 怎么判断一个类会生成 `Builder` 代码还是 `Set` 代码 ?**

A: 如果此类中有一个以 `Builder` 结尾的内部类则会生成 `Builder` 代码，倘若没有则会生成 `Set` 型代码，
`Set` 型代码则是通过寻找类中以 SetXXX 开头的方法来判断的。

如果生成的类是一个嵌套对象，请务必保证父对象和子对象都是 `Builder` 代码 或者 `Set` 型代码，因为判断时只会通过父对象来判断，
如果父对象是 `Builder` 代码而子对象是 `Set` 型代码则在生成子对象代码时出现一些错误代码，如果这种情况无法避免，可以将生成的错误代码删除，再点击子对象类单独生成。


**Q2: 通过方法参数和返回值进行代码生成时，是用什么逻辑做匹配 ?**

A: 首先我会拿到所有方法参数，依次将他们的变量名取出，接着对返回值的变量名做匹配，所以是通过变量名匹配。


**Q3: 在使用的过程中插件无反应，是什么问题 ?**

A: 这时你可以留意 `IDEA` 右下角气泡提示，如果它提示是绿色则代表你生成成功，如果是红色且可以看到 `BeanMapping` 开头的提示语则代表选中的对象不支持生成，
如果你看到了一个红色气泡但是并没有 `BeanMapping` 开头的提示语而是一个 `occurred error`，这代表我的插件出现了问题，你可以给我提 Issues。

Tip: 当代码还没有编译完成或者 `IDEA` 还没有准备完成，这时使用插件也有可能得到一个错误，因为插件依赖 `IDEA` 内部的上下文环境。


**Q4: 除了 Java，插件是否还支持其他语言 ?**

A: 截止到 2.0 版本，本插件只支持 Java。

<br/>

## Issues 指南

如您在使用过程中，遇到了什么问题或者有良好的使用建议，可以通过提 `issues` 方式告知我，
提 `issues` 之前请先组织好您的语言，尽量详细，如果是问题类的 `issues` 则需要附上 `IDEA` 版本。

最后，欢迎 **Star** 。
