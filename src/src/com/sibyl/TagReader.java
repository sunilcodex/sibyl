/* 
 *
 * Copyright (C) 2007-2008 sibyl project
 * http://code.google.com/p/sibyl/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sibyl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public abstract class TagReader {

    protected Map<String, String> cv; /* defining cv as a map as the class is abstract (and 
       so not instancianted). In the children classes, it will be instanciated as a Hashmap */
    
    // columns you'll find in the hashmap
    protected static final String[] cols = { Music.ALBUM.NAME, Music.GENRE.NAME, Music.SONG.TITLE, Music.SONG.TRACK, Music.ARTIST.NAME};
    
    protected static void skipSure(InputStream f, long n) throws IOException{
        while(n > 0){
            n -= f.skip(n);
        }
    }
    
    protected static void skipSure(InputStream f, int n) throws IOException{
        while(n > 0){
            n -= f.skip(n);
        }
    }

    public abstract Map<String, String> getValues();

}
