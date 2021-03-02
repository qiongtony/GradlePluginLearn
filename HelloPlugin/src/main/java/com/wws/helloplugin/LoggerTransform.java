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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

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
                for (File file : files){
                    try(FileInputStream is = new FileInputStream(file);
                        FileOutputStream os = new FileOutputStream(file.getPath())
                    ) {
                        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                        ClassVisitor cv = new ActivityClassVisitor(cw);

                        // 对class文件进行读取与解析
                        ClassReader cr = new ClassReader(is);
                        // 依次调用ClassVisitor接口的各个方法
                        cr.accept(cv, ClassReader.EXPAND_FRAMES);
                        byte[] bytes = cw.toByteArray();

                        os.write(bytes);
                        os.flush();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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

                try {
                    FileUtils.copyFile(jarInputFile, dest);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    /**
     * 遍历文件，返回class文件
     * @param file
     * @return
     */
    private List<File> filterClassFiles(File file) {
        List<File> classFiles = new ArrayList<>();
        if (file != null) {
            listFiles(file, classFiles, new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().endsWith(".class");
                }
            });
        }
        return classFiles;
    }

    private void listFiles(File file, List<File> result, FileFilter filter) {
        if (result == null || file == null) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    listFiles(child, result, filter);
                }
            }
        } else {
            if (filter == null || filter.accept(file)) {
                result.add(file);
            }
        }
    }
}
