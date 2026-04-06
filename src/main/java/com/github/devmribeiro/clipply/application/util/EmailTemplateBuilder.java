package com.github.devmribeiro.clipply.application.util;

public class EmailTemplateBuilder {

    public static String appointmentConfirmed(String clientName, String serviceName, String date, String professionalName, String cancelLink) {

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }" +
                ".container { background-color: #ffffff; padding: 20px; border-radius: 8px; }" +
                ".title { font-size: 20px; font-weight: bold; margin-bottom: 10px; }" +
                ".info { margin: 10px 0; }" +
                ".footer { margin-top: 20px; font-size: 12px; color: #777; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +

                "<div class='title'>Agendamento confirmado ✅</div>" +

                "<p>Olá, <strong>" + clientName + "</strong>!</p>" +

                "<p>Seu agendamento foi confirmado com sucesso. Seguem os detalhes:</p>" +

                "<div class='info'><strong>Serviço:</strong> " + serviceName + "</div>" +
                "<div class='info'><strong>Profissional:</strong> " + professionalName + "</div>" +
                "<div class='info'><strong>Data:</strong> " + date + "</div>" +

				"<p>Se precisar cancelar, clique <a href=" + cancelLink + ">aqui</a>.</p>" +

                "<div class='footer'>" +
                "Este é um email automático, não responda." +
                "</div>" +

                "</div>" +
                "</body>" +
                "</html>";
    }
}