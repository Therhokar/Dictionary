// Copyright 2011 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.hughes.android.dictionary.engine;


public enum EntryTypeName {

  WIKTIONARY_TITLE_SINGLE(true, true, null),
  WIKTIONARY_INFLECTD_FORM_SINGLE(false, true, null),


  ONE_WORD(true, true, null),
  MULTIROW_HEAD_ONE_WORD(true, true, null),
  MULTIROW_TAIL_ONE_WORD(false, true, null),

  WIKTIONARY_TITLE_MULTI(false, true, WIKTIONARY_TITLE_SINGLE),
  WIKTIONARY_TRANSLITERATION(),
  WIKTIONARY_INFLECTED_FORM_MULTI(false, true, WIKTIONARY_INFLECTD_FORM_SINGLE),
  WIKTIONARY_ENGLISH_DEF_WIKI_LINK(),
  WIKTIONARY_ENGLISH_DEF_OTHER_LANG(),
  WIKTIONARY_ENGLISH_DEF(),

  TWO_WORDS(),
  THREE_WORDS(),
  FOUR_WORDS(),
  FIVE_OR_MORE_WORDS(),
  WIKTIONARY_TRANSLATION_WIKI_TEXT(),
  WIKTIONARY_TRANSLATION_OTHER_TEXT(),

  WIKTIONARY_IS_FORM_OF_SOMETHING_ELSE(false, true, null),

  MULTIROW_HEAD_MANY_WORDS(),
  MULTIROW_TAIL_MANY_WORDS(),
  WIKTIONARY_EXAMPLE(),
  WIKTIONARY_BASE_FORM_SINGLE(),  // These two should be eligible for removal if the links are otherwise present.
  WIKTIONARY_BASE_FORM_MULTI(false, false, WIKTIONARY_BASE_FORM_SINGLE),
  PART_OF_HYPHENATED(),
  BRACKETED(),
  PARENTHESIZED(),
  WIKTIONARY_TRANSLATION_SENSE(),
  SEE_ALSO(), 
  ;

  final boolean mainWord;
  final boolean overridesStopList;
  final EntryTypeName singleWordInstance;
  
  private EntryTypeName() {
    this(false, false, null);
  }

  private EntryTypeName(final boolean mainWord, final boolean overridesStopList, final EntryTypeName singleWordInstance) {
    this.mainWord = mainWord;
    this.overridesStopList = overridesStopList;
    this.singleWordInstance = singleWordInstance == null ? this : singleWordInstance;
  }

}
