package com.wxl.agent.converter;

import com.wxl.agent.model.entity.ChatSession;
import com.wxl.agent.model.vo.ChatSessionVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatSessionConverter {

    // 转换单个对象
    ChatSessionVO toVo(ChatSession chatSession);

    // 转换列表 (MapStruct 会自动遍历内部元素调用 toVo)
    List<ChatSessionVO> toVoList(List<ChatSession> chatSessionList);
}