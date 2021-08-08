# 构建最新版 MiraiBot

**目前只制作了对于 Linux / Mac 的一键编译脚本**

## 在此之前

### 首先你要确定你是否已安装以下 Package：
- unzip
- wget
- OpenJDK 11 及以上

### 如何安装这些 Package：

你可以使用以下命令安装 `unzip` 和 `wget`：
```shell
yum -y install wget unzip
或
apt install -y wget unzip
```
要安装 `Open JDK`，请前往 [AdoptOpenJDK](https://adoptopenjdk.net/releases.html) 查看适合你的系统的 `Open JDK`，

然后下载其 `.tar.gz` 文件，下载完成后 `cd` 到该目录，执行以下命令：
```shell
mkdir -p /usr/local/AdoptOpenJDK
tar -xzvf [你下载的 JDK 文件名].tar.gz -C /usr/local/AdoptOpenJDK/
```
接下来通过 `ls` 命令查看 `Open JDK` 的根目录：
```shell
ls /usr/local/AdoptOpenJDK
```
运行后会列出 `Open JDK` 中的所有文件及文件夹，

一般情况下只有一个目录，记住这个目录名

然后开始配置环境变量：
```shell
vi /etc/profile
```
然后按下 `i` 键，通过方向键滚动到文件底部，然后输入以下内容：
```
export JAVA_HOME=/usr/local/AdoptOpenJDK/[刚才记下的目录名]
export CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/
export PATH=$PATH:$JAVA_HOME/bin
```

现在，你的 `Open JDK` 应该已经安装成功了，使用以下命令来检查是否安装正确：
```shell
java -version
```
如果正确的显示了类似于 `AdoptOpenJDK 11` 的字样，就代表安装成功了！

## 下一步
在确保安装了这些 Package 之后，使用 `wget` 下载这个自动构建脚本：
```
wget https://ghproxy.com/https://github.com/XIAYM-gh/miraibot-plugins/releases/download/0.1/MiraiBotBuilder.zip
```

接下来，运行这些命令:
```shell
unzip MiraiBotBuilder.zip
cd MiraiBotBuilder
bash build.sh
```

运行完成之后，将会在 `MiraiBotBuilder` 文件夹里生成一个 `MiraiBot.jar`，你可以把它覆盖到你的 `MiraiBot` 工作目录
