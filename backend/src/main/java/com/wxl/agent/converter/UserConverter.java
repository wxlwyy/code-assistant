package com.wxl.agent.converter;

import com.wxl.agent.model.entity.User;
import com.wxl.agent.model.vo.LoginUserVO;
import org.mapstruct.Mapper;

/**
 * 用户对象编译期转换器（MapStruct 工业级标配）
 * 当你打包或编译时，MapStruct 会自动在 target/generated-sources 目录下生成纯 set/get 的高效实现类。
 */
// componentModel = "spring" 代表将生成的实现类自动注册为 Spring 的 Bean
@Mapper(componentModel = "spring")
public interface UserConverter {

    /**
     * 将 User 实体自动转换为脱敏后的已登录用户视图
     */
    LoginUserVO toLoginUserVO(User user);
}