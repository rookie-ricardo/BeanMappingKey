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

以上生成逻辑中，若有 `Builder` 内部类则优先生成 `Builder` 代码，其次生成 `set` 型代码。

<br/>

## Issues 指南

如您在使用过程中，遇到了什么问题或者有良好的使用建议，可以通过提 `issues` 方式告知我，
提 `issues` 之前请先组织好您的语言，尽量详细，如果是问题类的 `issues` 则需要附上 `IDEA` 版本。

最后，欢迎 **Star** 。
