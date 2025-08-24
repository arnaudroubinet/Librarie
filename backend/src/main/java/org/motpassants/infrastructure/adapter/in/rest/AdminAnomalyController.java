package org.motpassants.infrastructure.adapter.in.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.motpassants.domain.core.model.BookAnomaly;
import org.motpassants.domain.port.in.BookAnomalyUseCase;

import java.util.List;

@Path("/api/admin/anomalies")
@Tag(name = "Admin", description = "Administrative diagnostics")
public class AdminAnomalyController {

    @Inject
    BookAnomalyUseCase bookAnomalyUseCase;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List book anomalies", description = "Scan and list data anomalies for books")
    public List<BookAnomaly> listAnomalies() {
        return bookAnomalyUseCase.scanAnomalies();
    }
}
