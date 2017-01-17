package com.cubedhost.fir;

import java.util.List;
import java.util.Map;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class InfoClassVisitor extends ClassVisitor {

    final List<Map<String, Object>> resultList;
    private InfoAnnotationVisitor currentAnnotationVisitor = null;

    public InfoClassVisitor(List<Map<String, Object>> resultList) {
        super(Opcodes.ASM5);
        this.resultList = resultList;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        checkCurrentAnnotation();

        if (desc.equals("Lnet/minecraftforge/fml/common/Mod;") || desc.equals("Lcpw/mods/fml/common/Mod;")) {
            return currentAnnotationVisitor = new InfoAnnotationVisitor();
        }

        return super.visitAnnotation(desc, visible);
    }

    @Override
    public void visitEnd() {
        checkCurrentAnnotation();
    }

    private void checkCurrentAnnotation() {
        if (currentAnnotationVisitor == null) {
            return;
        }

        Map<String, Object> values = currentAnnotationVisitor.getValues();
        if (values.containsKey("modid") && values.containsKey("name") && values.containsKey("version")) {
            values.put("valid", true);
        } else {
            values.put("valid", false);
        }

        resultList.add(values);
    }
}
