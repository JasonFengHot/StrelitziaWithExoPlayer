/*
* Copyright (C) 2015 Vincent Mi
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

package tv.ismar.searchpage.weight;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;


public final class ReflectionTransformationBuilder {
    private boolean isHorizontal;

    public ReflectionTransformationBuilder setIsHorizontal(boolean isHorizontal) {
        this.isHorizontal = isHorizontal;
        return this;
    }


    public Transformation build() {
        return new Transformation() {
            @Override
            public Bitmap transform(Bitmap source) {
                Bitmap transformed = ReflectionDrawable.fromBitmap(source, isHorizontal)
                        .toBitmap();
                if (!source.equals(transformed)) {
                    source.recycle();
                }
                return transformed;
            }

            @Override
            public String key() {
                return "";
            }
        };
    }
}
