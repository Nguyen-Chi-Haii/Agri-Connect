package vn.agriconnect.API.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostInteractionResponse {
    private int likeCount;
    private int commentCount;
    private boolean isLiked;
}
