package com.example.bankcards.controller.frontend;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/error")
public class ErrorControllerImpl implements ErrorController {
    private static final Logger APP_LOG = LoggerFactory.getLogger("APP_LOG");

    private static final Map<Integer, ErrorInfo> ERROR_INFO = Map.of(
            400, new ErrorInfo("Bad Request", "Неверный запрос. Проверьте введенные данные."),
            401, new ErrorInfo("Unauthorized", "Требуется авторизация для доступа к этой странице."),
            403, new ErrorInfo("Forbidden", "У вас нет прав доступа к этому ресурсу."),
            404, new ErrorInfo("Not Found", "Запрашиваемая страница не найдена."),
            405, new ErrorInfo("Method Not Allowed", "Метод запроса не поддерживается."),
            500, new ErrorInfo("Internal Server Error", "Произошла внутренняя ошибка сервера. Попробуйте позже."),
            502, new ErrorInfo("Bad Gateway", "Ошибка шлюза. Повторите попытку позже."),
            503, new ErrorInfo("Service Unavailable", "Сервис временно недоступен. Ведутся технические работы.")
    );

    @GetMapping
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;
        ErrorInfo errorInfo = ERROR_INFO.getOrDefault(statusCode,
                new ErrorInfo("Error", "Произошла непредвиденная ошибка"));

        if (statusCode >= 500) {
            APP_LOG.error("Server error {}: {}", statusCode, message, exception);
        }

        model.addAttribute("status", statusCode);
        model.addAttribute("title", errorInfo.title());
        model.addAttribute("message", errorInfo.message());
        model.addAttribute("timestamp", LocalDateTime.now());

        return "error";
    }

    private record ErrorInfo(String title, String message) {}
}