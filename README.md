# mybatis-queryHelper
- 生成目录
- Google开发了一个AutoService，用来生成 META-INF/services/javax.annotation.processing.Processor 文件的，你只需要在你定义的注解处理器上添加 @AutoService(Processor.class) 就可以了

### 处理器的属性方法介绍
- Messager:主要起一个日志的作用 ，为注解处理器提供了一种报告错误消息，警告信息和其他消息的方式。它不是注解处理器开发者的日志工具，是用来给那些使用了你的注解处理器的第三方开发者显示信息的。Kind.ERROR，就是错误信息的级别。
- Elements:一个用来处理Elemnet的工具类
- Element代表程序中的元素，比如包、类、属性、方法。
- Types:一个用来处理TypeMirror的工具类
- Filer:可以用来创建文件。