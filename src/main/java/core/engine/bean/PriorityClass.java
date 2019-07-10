package core.engine.bean;

import java.util.Set;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 17:02
 * @Description:
 */
public class PriorityClass {

    private Integer priorty;
    private Class clazz;
    private Set<Class> interfaces;

    public Integer getPriorty() {
        return priorty;
    }

    public void setPriorty(Integer priorty) {
        this.priorty = priorty;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public Set<Class> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(Set<Class> interfaces) {
        this.interfaces = interfaces;
    }
}
