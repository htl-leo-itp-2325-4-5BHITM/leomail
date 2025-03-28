package at.htlleonding.leomail.model.dto;
public record AttachmentResponseDTO(
        Long id,
        String fileName,
        String contentType,
        String downloadUrl,
        long size
) {}
