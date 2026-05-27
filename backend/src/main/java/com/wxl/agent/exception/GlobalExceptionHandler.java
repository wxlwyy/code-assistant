package com.wxl.agent.exception;

import com.wxl.agent.common.BaseResponse;
import com.wxl.agent.common.ErrorCode;
import com.wxl.agent.common.ResultUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //  业务异常 (彻底抛弃繁琐的堆栈解析！)
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e, HttpServletRequest request) {
        log.warn("\n[Business Exception] \n" +
                        "Request URI: {}\n" +
                        "Code: {}\n" +
                        "Message: {}\n",
                request.getRequestURI(), e.getCode(), e.getMessage());
        return new BaseResponse<>(e.getCode(), null, e.getMessage());
    }

    //  系统异常 (这个保留堆栈，因为是真 Bug，并加上 URI 方便复现)
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e, HttpServletRequest request){
        log.error("\n[System Error] \n" +
                        "Request URI: {}\n" +
                        "Message: 系统发生未知异常！\n",
                request.getRequestURI(), e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    /**
     * 拦截 [空请求体] 异常，会导致RequestBody注解解析json报错(HttpMessageNotReadableException),和validated注解无关
     * 这个异常是spring MVC在解析请求体这一步直接报错了，他是spring底层负责把Json转化成Java对象的组件，
     * 抛异常的时机非常早，还没进入到controller方法。更不用说参数校验
     * 触发场景：POST/PUT 请求连 {} 都没有（相当于啥都没穿），或者 JSON 格式写错了（比如少个逗号）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BaseResponse<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e,
                                                                 HttpServletRequest request) {
        log.warn("\n[Parameter Empty] \n" +
                        "Request URI: {}\n" +
                        "Message: 请求体为空或格式错误\n",
                request.getRequestURI());
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, "请求体不可为空");
    }

    /**
     * 拦截 [DTO 校验] 异常 (MethodArgumentNotValidException)，和validated注解有关
     * 触发场景：@Validated 校验失败（例如 @NotBlank, @Size 等不通过）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)  //org.springframework.web.bind.MethodArgumentNotValidException;
    public BaseResponse<?> handleValidationException(MethodArgumentNotValidException e,
                                                     HttpServletRequest request) {
        BindingResult bindingResult = e.getBindingResult();
        String errorMessage = "参数错误";
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldErrors().get(0);
            errorMessage = fieldError.getDefaultMessage();
        }

        log.warn("\n[Validation Error] \n" +
                        "Request URI: {}\n" +
                        "Message: {}\n",
                request.getRequestURI(), errorMessage);

        return ResultUtils.error(ErrorCode.PARAMS_ERROR, errorMessage);
    }

    /**
     * 拦截 [GET/单参数] 校验异常 (ConstraintViolationException)，和validated注解有关
     * 触发场景：@RequestParam 标注的参数不符合 @NotNull, @Min 等
     */
    @ExceptionHandler(ConstraintViolationException.class)  // springboot2用javax的，3用jakarta的
    public BaseResponse<?> handleConstraintViolationException(ConstraintViolationException e,
                                                              HttpServletRequest request) {
        // 直接从 ConstraintViolation 对象中提取消息，比字符串截取更可靠
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("\n[Constraint Violation] \n" +
                        "Request URI: {}\n" +
                        "Message: {}\n",
                request.getRequestURI(), message);

        return ResultUtils.error(ErrorCode.PARAMS_ERROR, message);
    }

    /**
     * 补充 1：拦截 [请求方式错误] 异常 (HttpRequestMethodNotSupportedException)
     * 触发场景：你的接口写的是 @PostMapping，但前端如果发了个 GET 请求。
     * 处理逻辑：这属于前端/用户的调用错误，不是后端 Bug。所以记 warn 日志，不打堆栈。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public BaseResponse<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e,
                                                                        HttpServletRequest request) {
        log.warn("\n[Method Not Supported]\n" +
                        "Request URI: {}\n" +
                        "Message: {}\n",
                request.getRequestURI(), e.getMessage());
        // 返回参数错误，并明确提示请求方式不对
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, "请求方式错误，请检查 GET/POST 等方法");
    }

    /**
     * 补充 2：最终兜底(Exception)
     * 触发场景：第三方依赖库抛出了极其罕见的受检异常（非 RuntimeException），且没有任何一个前置方法能拦住它。
     * 处理逻辑：既然漏到这里了，说明是超预期的真 Bug！必须记 error，并且传入 `e` 打印完整堆栈，方便日后排查。
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse<?> handleUnknownException(Exception e, HttpServletRequest request) {
        // 1. 【核心优化】如果是 Spring 内置的 Web 异常（404, 405 等），直接原样抛出，让 Spring 自己处理
        // 这样就不会触发我们的 ERROR 日志，更不会让 Knife4j 报错
        if (e instanceof org.springframework.web.servlet.resource.NoResourceFoundException) {
            // 对于静态资源找不到的请求，直接返回 404，不记任何日志
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "资源不存在");
        }

        // 2. 【日志格式优化】修正你提到的“横线在堆栈后面”的问题
        // 我们把分隔线放在报错信息的前面和中间，把 e 放在最后作为详细附件
        log.error("\n[SYSTEM CRASH START]\n" +
                        "Request URI : {}\n" +
                        "Error Type  : {}\n" +
                        "Short Msg   : {}\n",
                request.getRequestURI(), e.getClass().getName(), e.getMessage(), e);

        // 3. 对外始终保持体面
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务器开小差了，请稍后再试");
    }
}