package dev.piloto;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTest {

    @Test
    void estructuraDeModulosEsValida() {
        ApplicationModules.of(PilotoApplication.class).verify();
    }
}
