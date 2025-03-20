package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.SummaryTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SummaryTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Summary.class);
        Summary summary1 = getSummarySample1();
        Summary summary2 = new Summary();
        assertThat(summary1).isNotEqualTo(summary2);

        summary2.setId(summary1.getId());
        assertThat(summary1).isEqualTo(summary2);

        summary2 = getSummarySample2();
        assertThat(summary1).isNotEqualTo(summary2);
    }
}
