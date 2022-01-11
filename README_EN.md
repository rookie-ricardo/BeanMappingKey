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
  One-click generation of field mapping code between two entity classes, which is used to replace tools such as BeanUtil and MapStruct.
  <br>
</p>

<br/>

## Installation Guide

- Using IDE built-in plugin system on Windows:
    - <kbd>File</kbd> > <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>Browse repositories...</kbd> > <kbd>Search for "BeanMappingKey"</kbd> > <kbd>Install Plugin</kbd>
- Using IDE built-in plugin system on MacOs:
    - <kbd>Preferences</kbd> > <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>Browse repositories...</kbd> > <kbd>Search for "BeanMappingKey"</kbd> > <kbd>Install Plugin</kbd>
- Manually:
    - Download the [latest release](https://github.com/rookie-ricardo/BeanMappingKey/releases/latest) and install it manually using <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Install plugin from disk...</kbd>

PS: For the time being, only versions above 2020 are supported, no need to restart ide after installation.

<br/>

## Start Guide

In the process of Java development, many packaging objects such as BO, VO, DTO are often used, and there are often only two or three field differences between them.

Converting them to each other is a time-consuming and labor-intensive physical activity, so tools such as `BeanUtil` and `MapStruct` are derived from the Java ecosystem.

However, `BeanUtil` cannot view the details of object conversion, and `MapStruct` sometimes encounters unsatisfactory object conversion, so you still need to manually write object conversion code.
<br/>

BeanMappingKey was developed to solve this type of demand. It (temporarily) has three usages:

- Select a method with return value and parameters, use the shortcut key `Ctrl+M` or right click on BeanMappingKey,
The relevant conversion code can be inserted automatically.

- Select a variable and use the shortcut key `Ctrl+M` or right-click on BeanMappingKey to automatically generate the conversion code to the pasteboard.
- Select a class and use the shortcut key `Ctrl+M` or right-click on BeanMappingKey to automatically generate the conversion code to the pasteboard.

In the above generation logic, if there is a `Builder` internal class, the `Builder` code will be generated first, and then the `set` type code will be generated.

<br/>

## Issues Guide

If you encounter any problems or have good usage suggestions during use, you can tell me by submitting `issues`,
Before submitting `issues`, please organize your language and be as detailed as possible. If it is an issue of `issues`, you need to attach the `IDEA` version.

Finally, welcome **Star**.
