# Docker image for springboot application
# VERSION 0.0.1
# Author: zks

# 构建镜像基于哪个镜像
FROM openjdk:8
# 镜像维护者姓名或邮箱地址
MAINTAINER zks
# 为 RUN、CMD、ENTRYPOINT、COPY 和 ADD 设置工作目录，就是切换目录
WORKDIR /
#系统编码
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
# 拷贝文件或目录到容器中，跟ADD类似，但不具备自动下载或解压的功能
COPY llbPBFT.jar llbPBFT.jar
#启动容器时的进程
#ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS $ENV_OPTS $REMOTE2 -jar ko-assy-db.jar" ]
CMD ["java", "-version"]
# 容器启动时执行的命令
ENTRYPOINT ["java","-jar","/llbPBFT.jar"]
#暴露11109端口
EXPOSE 11109
# 声明一个挂载点，容器内此路径会对应宿主机的某个文件夹
# 一般的使用场景为需要持久化存储数据时
# 容器使用的是AUFS，这种文件系统不能持久化数据，当容器关闭后，所有的更改都会丢失。
# 所以当数据需要持久化时用这个命令。
# VOLUME 指令创建的挂载点，无法指定主机上对应的目录，是自动生成的。
VOLUME /data/app/llbPBFT-ms/logs