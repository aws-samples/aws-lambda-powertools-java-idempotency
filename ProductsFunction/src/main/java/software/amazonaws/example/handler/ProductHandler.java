package software.amazonaws.example.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazonaws.example.dao.DynamoProductDao;
import software.amazonaws.example.dao.ProductDao;
import software.amazonaws.example.entity.Product;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ProductHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger logger = LogManager.getLogger(ProductHandler.class);
    private ProductDao productDao;
    private ObjectMapper objectMapper;

    public ProductHandler() {
        this.productDao = new DynamoProductDao();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, final Context context) {

        try {
            logger.info("Received Input Event :: " + objectMapper.writeValueAsString(input));
        } catch (JsonProcessingException je) {
            logger.error(je.getMessage());
        }

        if (input.getHttpMethod().equals(SdkHttpMethod.PUT.name())) {
            return addResponseHeaders(processPutEvent(input));
        }
        if (input.getHttpMethod().equals(SdkHttpMethod.POST.name())) {
            return addResponseHeaders(processPostEvent(input));
        }
        if (input.getHttpMethod().equals(SdkHttpMethod.GET.name())) {
            if (input.getPathParameters() == null) {
                return addResponseHeaders(processGetAllEvent(input));
            } else {
                return addResponseHeaders(processGetByIdEvent(input));
            }
        }
        if (input.getHttpMethod().equals(SdkHttpMethod.DELETE.name())) {
            return addResponseHeaders(processDeleteEvent(input));
        }

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withStatusCode(500);
        return addResponseHeaders(response);

    }

    private APIGatewayProxyResponseEvent addResponseHeaders(APIGatewayProxyResponseEvent response) {
        logger.info("Adding Response Headers");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        return response.withHeaders(headers);
    }

    private APIGatewayProxyResponseEvent processPutEvent(APIGatewayProxyRequestEvent input) {
        logger.info("Processing Put Event");
        try {
            String id = input.getPathParameters().get("id");
            String jsonPayload = input.getBody();
            Product product = objectMapper.readValue(jsonPayload, Product.class);
            if (!product.getId().equals(id)) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(HttpStatusCode.BAD_REQUEST)
                        .withBody("{\"error\":\"Product ID in the body does not match path parameter\"}");
            }
            productDao.putProduct(product);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.CREATED)
                    .withBody("{\"message\":\"Product with id = " + id + " created\"}");
        } catch (Exception je) {
            logger.error("Error while processing put event :: " , je.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
                    .withBody("{\"error\":\"Internal Server Error :: " + je.getMessage() + "\"}");
        }
    }

    private APIGatewayProxyResponseEvent processPostEvent(APIGatewayProxyRequestEvent input) {
        logger.info("Processing Post Event");
        try {
            String jsonPayload = input.getBody();
            Product product = objectMapper.readValue(jsonPayload, Product.class);
            String productId = productDao.createProduct(product);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.CREATED)
                    .withBody("{\"message\":\"Product with id = " + productId + " created\"}");
        } catch (Exception je) {
            logger.error("Error while processing Post Event :: " , je.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
                    .withBody("{\"error\":\"Internal Server Error :: " + je.getMessage() + "\"}");
        }
    }

    private APIGatewayProxyResponseEvent processGetAllEvent(APIGatewayProxyRequestEvent input) {
        logger.info("Processing Get All Event");
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.OK)
                    .withBody(objectMapper.writeValueAsString(productDao.getAllProduct()));
        } catch (JsonProcessingException je) {
            logger.error("Error while processing Get All event :: " , je.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
                    .withBody("{\"error\":\"Internal Server Error :: " + je.getMessage() + "\"}");
        }
    }

    private APIGatewayProxyResponseEvent processGetByIdEvent(APIGatewayProxyRequestEvent input) {
        logger.info("Processing Get By Id Event");
        try {
            String id = input.getPathParameters().get("id");
            Optional<Product> product = productDao.getProduct(id);
            if (product.isEmpty()) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(HttpStatusCode.NOT_FOUND)
                        .withBody("{\"error\":\"Product with id = " + id + " not found\"}");
            }
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.OK)
                    .withBody(objectMapper.writeValueAsString(product.get()));
        } catch (Exception je) {
            logger.error("Error while processing Get By Id event :: " , je.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
                    .withBody("{\"error\":\"Internal Server Error :: " + je.getMessage() + "\"}");
        }
    }

    private APIGatewayProxyResponseEvent processDeleteEvent(APIGatewayProxyRequestEvent input) {
        logger.info("Processing Delete Event");
        try {
            String id = input.getPathParameters().get("id");
            Optional<Product> product = productDao.getProduct(id);
            if (product.isEmpty()) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(HttpStatusCode.NOT_FOUND)
                        .withBody("{\"error\":\"Product with id = " + id + " not found\"}");
            }
            productDao.deleteProduct(id);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.OK)
                    .withBody("{\"message\":\"Product with id = " + id + " deleted\"}");
        } catch (Exception je) {
            logger.error("Error while processing delete event :: " , je.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
                    .withBody("{\"error\":\"Internal Server Error :: " + je.getMessage() + "\"}");
        }
    }
}