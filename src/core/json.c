/*
 * Droidspaces v7 - High-performance Container Runtime
 *
 * Copyright (C) 2026 ravindu644 <droidcasts@protonmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

#include "droidspaces.h"

/* JSON output. We only ever write flat objects of strings and integers, so
 * these two functions and ds_json_status() in status/collect.c are the whole
 * encoder. */

void ds_json_str(const char *key, const char *val, int *first) {
  printf("%s\"%s\":\"", *first ? "" : ",", key);
  *first = 0;
  /* Batched instead of one printf per character: a status tick escapes every
   * string it prints, and each call took the stdio lock again. The output is
   * byte for byte the same. */
  char out[256];
  size_t n = 0;
  for (const unsigned char *p = (const unsigned char *)(val ? val : ""); *p;
       p++) {
    /* "\u00xx" is six bytes plus the terminator snprintf adds. */
    if (n + 7 > sizeof(out)) {
      fwrite(out, 1, n, stdout);
      n = 0;
    }
    if (*p == '"' || *p == '\\') {
      out[n++] = '\\';
      out[n++] = (char)*p;
    } else if (*p < 0x20) {
      n += (size_t)snprintf(out + n, sizeof(out) - n, "\\u%04x", *p);
    } else {
      out[n++] = (char)*p;
    }
  }
  if (n)
    fwrite(out, 1, n, stdout);
  putchar('"');
}

void ds_json_int(const char *key, long long val, int *first) {
  printf("%s\"%s\":%lld", *first ? "" : ",", key, val);
  *first = 0;
}
