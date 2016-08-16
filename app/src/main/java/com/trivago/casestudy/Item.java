package com.trivago.casestudy;

/**
 * Created by Matthias on 15.08.16 at 19:11.
 */
public class Item {

    String title, year;


    class ids {
        String trakt, slug, imdb, tmdb;
    }

    class images {
        class fanart {
            String full, medium, thumb;
        }

        class poster {
            String full, medium, thumb;
        }

        class logo {
            String full;
        }

        class clearart {
            String full;
        }

        class banner {
            String full;
        }

        class thumb {
            String full;
        }
    }
}

