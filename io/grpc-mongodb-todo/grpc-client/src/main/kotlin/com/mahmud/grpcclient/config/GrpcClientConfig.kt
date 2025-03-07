package com.mahmud.grpcclient.config
import com.mahmud.grpcclient.TodoServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GrpcClientConfig {

    @Bean
    fun todoServiceStub(): TodoServiceGrpc.TodoServiceBlockingStub {
        val channel: ManagedChannel = ManagedChannelBuilder
            .forAddress("localhost", 9090) // Connects to your gRPC server
            .usePlaintext() // No TLS for simplicity
            .build()
        return TodoServiceGrpc.newBlockingStub(channel)
    }
}