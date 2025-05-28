package cn.chauncy;

import cn.chauncy.dao.mapper.PlayerDataMapper;
import cn.chauncy.utils.thread.ConsoleService;
import com.baomidou.mybatisplus.core.toolkit.reflect.GenericTypeUtils;
import com.baomidou.mybatisplus.core.toolkit.reflect.TypeParameterResolver;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;


public class GameStarter {

    private final ServiceManager manager;

    @Inject
    public GameStarter(Set<Service> services) {
        this.manager = new ServiceManager(services);
    }

    public void start() {
        manager.startAsync();
    }

    public static void main(String[] args) {
        // 临时代码修复防止报错
        fixMybatisPlusOnAbsenceSpringDependencyIssue();

        ConsoleService.addHandlerClass(ConsoleHandler.class);

        Injector injector = Guice.createInjector(GameModule.INSTANCE);
        GameStarter starter = injector.getInstance(GameStarter.class);
        starter.start();


        PlayerDataMapper instance = injector.getInstance(PlayerDataMapper.class);
        int i = instance.deleteById(1001);
        System.out.println(i);
    }

    private static void fixMybatisPlusOnAbsenceSpringDependencyIssue() {
        GenericTypeUtils.setGenericTypeResolver((Class<?> clazz, Class<?> genericIfc) -> {
            Map<TypeVariable<?>, Type> map = new HashMap<>();
            new TypeParameterResolver(map){}.visitType(clazz);
            List<Class<?>> result  = new ArrayList<>(1);
            for (TypeVariable<? extends Class<?>> typeParameter : genericIfc.getTypeParameters()) {
                Type res = map.get(typeParameter);
                while (res instanceof TypeVariable<?>) {
                    res = map.get(res);
                }
                result.add((Class<?>) res);
            }
            return result.toArray(new Class<?>[0]);
        });
    }
}
