package com.wws.helloplugin;

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class LoggerTransform extends Transform {
    @Override
    public String getName() {
        return "ac_logger";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        // 转换器处理的数据类型：我们只需要处理class内容，可能是jar或者是文件夹
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        // 转换器的作用域：Project的内容
        return TransformManager.PROJECT_ONLY;
    }

    @Override
    public boolean isIncremental() {
        // 是否支持增量
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        // 实现转换操作
        TransformOutputProvider provider = transformInvocation.getOutputProvider();
        if (provider == null){
            return;
        }
        // 1、删除原编译文件
        provider.deleteAll();

        // 2、转换
        Collection<TransformInput> transformInputs = transformInvocation.getInputs();

        transformInputs.forEach(transformInput -> {
            // 2.1目录
            transformInput.getDirectoryInputs().forEach(directoryInput -> {
                File directoryInputFile = directoryInput.getFile();
                List<File> files = filterClassFiles(directoryInputFile);
                // 输入目录复制倒目标目录
                File dest = provider.getContentLocation(
                        directoryInput.getName(),
                        directoryInput.getContentTypes(),
                        directoryInput.getScopes(),
                        Format.DIRECTORY
                );
                try {
                    FileUtils.copyDirectory(directoryInput.getFile(), dest);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // 处理jar输入
            transformInput.getJarInputs().forEach(jarInput -> {
                File jarInputFile = jarInput.getFile();
                File dest = provider.getContentLocation(
                        jarInput.getName(),
                        jarInput.getContentTypes(),
                        jarInput.getScopes(),
                        Format.JAR
                );
            });
        });
    }
}
