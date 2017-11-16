package rocks.massi.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PagesInformation {
    final int totalPages;
    final int pageSize;
}
