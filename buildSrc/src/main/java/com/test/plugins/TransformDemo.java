package com.test.plugins;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.BaseExtension;
import com.android.utils.FileUtils;

import org.apache.commons.codec.digest.DigestUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class TransformDemo extends Transform {

    final BaseExtension baseExtension;

    TransformDemo(BaseExtension baseExtension) {
        this.baseExtension = baseExtension;
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        Set<QualifiedContent.ContentType> set = new HashSet<>();
        set.add(QualifiedContent.DefaultContentType.CLASSES);
        return set;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        Set<QualifiedContent.Scope> set = new HashSet<>();
        set.add(QualifiedContent.Scope.PROJECT);
        return set;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    void injectTransform(String path, BaseExtension baseExtension) throws Exception {
        System.out.println("path = " + path);
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        // 加入当前路径
        super.transform(transformInvocation);

        //
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        outputProvider.deleteAll();


        Collection<TransformInput> transformInputs = transformInvocation.getInputs();
        transformInputs.forEach(transformInput -> {
            Collection<DirectoryInput> directoryInputs = transformInput.getDirectoryInputs();
            directoryInputs.forEach(directoryInput -> {
                try {
                    //插代码
//                    injectTransform(directoryInput.getFile().getAbsolutePath(), baseExtension);
                    //查看字节码生成的位置
//                    System.out.println("directoryInput = " + directoryInput.getFile().getAbsolutePath());
//                    //直接复制输出到对应的目录
                    String dirName = directoryInput.getName();
                    File src = directoryInput.getFile();
                    func(src);
                    System.out.println("目录 = " + src);
//                    String md5Name = DigestUtils.md5Hex(src.getAbsolutePath());
//                    File dest = outputProvider.getContentLocation(dirName + md5Name, directoryInput.getContentTypes()
//                            , directoryInput.getScopes(), Format.DIRECTORY);
//                    FileUtils.copyDirectory(src,dest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //拷贝处理完的代码，必做
                copyDirectory(directoryInput, transformInvocation.getOutputProvider());
            });
            Collection<JarInput> jarInputs = transformInput.getJarInputs();
            jarInputs.forEach(jarInput ->
            {
                //拷贝Jar包，必做
                copyJar(jarInput, transformInvocation.getOutputProvider());
            });
        });
    }

    private static void func(File file) {
        File[] fs = file.listFiles();
        for (File f : fs) {
            if (f.isDirectory())    //若是目录，则递归打印该目录下的文件
                func(f);
            if (f.isFile()) {
                //若是文件，直接打印
                if (f.toString().contains("MainActivity")) {
                    try {
                        InputStream is = new FileInputStream(f);
                        byte[] bytes = IOUtils.read(is);
                        System.out.println("bytes = " + bytes.toString());
                        is.close();
                        //下面就是ASM框架的使用
                        ClassReader classReader = new ClassReader(bytes);
                        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                        classReader.accept(new MyClassVisitor(classWriter),0);
                        byte[] bytes1 = classWriter.toByteArray();
                        String override = "/Users/zhangshunwei/AndroidStudioProjects/TestPluginOne/app/build/intermediates/javac/debug/classes/com/example/testpluginone/MainActivity.class";
                        FileOutputStream fileOutputStream = new FileOutputStream(override);
                        fileOutputStream.write(bytes1);
                        fileOutputStream.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private void copyJar(JarInput jarInput, TransformOutputProvider outputProvider) {
        String jarName = jarInput.getName();
        System.out.println("jar = " + jarInput.getFile().getAbsolutePath());
        String md5Name = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath());
        if (jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length() - 4);
        }
        File dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
        try {
            FileUtils.copyFile(jarInput.getFile(), dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyDirectory(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        File dest = outputProvider.getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
        try {
            FileUtils.copyDirectory(directoryInput.getFile(), dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}