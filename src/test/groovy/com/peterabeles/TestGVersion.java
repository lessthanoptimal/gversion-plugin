package com.peterabeles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
class TestGVersion {
    @Test
    void git_version() {
        check(2,17,1,GVersion.parseGitVersion("git version 2.17.1"));
        check(1,7,10,GVersion.parseGitVersion("git version 1.7.10.4"));
        check(1,7,0,GVersion.parseGitVersion("git version 1.7"));
        check(1,0,0,GVersion.parseGitVersion("git version 1"));
    }

    private void check(int a, int b , int c , int[] version ) {
        assertEquals(a,version[0]);
        assertEquals(b,version[1]);
        assertEquals(c,version[2]);
    }
}