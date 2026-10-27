package com.majstro.psms.backend.mapper;

import com.majstro.psms.backend.dto.ZoomTranscriptDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ZoomTranscriptMapper {
    /**
     * Combines all transcript segments into a single string and returns a ZoomTranscriptDto.
     *
     * @param meetingId          The meeting ID.
     * @param transcriptSegments List of transcript segment maps (from fetchMeetingTranscripts).
     * @return ZoomTranscriptDto with combined transcript text.
     */
    public ZoomTranscriptDto toDto(String meetingId, List<Map<String, Object>> transcriptSegments) {
        StringBuilder combined = new StringBuilder();
        for (Map<String, Object> segment : transcriptSegments) {
            // Try to extract the transcript text from the segment
            // The key may vary depending on Zoom's API response, e.g., "transcript", "text", etc.
            Object text = segment.get("transcript");
            if (text == null) text = segment.get("text");
            if (text != null) {
                combined.append(text).append("\n");
            }
        }
        ZoomTranscriptDto dto = new ZoomTranscriptDto();
        dto.setMeetingId(meetingId);
        dto.setTranscriptText(combined.toString().trim());
        return dto;
    }
}
