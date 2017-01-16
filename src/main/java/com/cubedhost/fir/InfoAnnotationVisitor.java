package com.cubedhost.fir;

import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

public class InfoAnnotationVisitor extends AnnotationVisitor {

    private final Map<String, Object> values = new HashMap<>();

    public InfoAnnotationVisitor() {
        super(Opcodes.ASM5);
    }

    @Override
    public void visit(String name, Object value) {
        values.put(name, value);
    }

    public Map<String, Object> getValues() {
        return values;
    }
}
