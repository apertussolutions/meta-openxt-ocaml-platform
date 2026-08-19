SUMMARY = "Camomile is a Unicode library for ocaml."
DESCRIPTION = "Camomile provides Unicode character type, UTF-8, UTF-16, \
UTF-32 strings, conversion to/from about 200 encodings, collation and \
locale-sensitive case mappings, and more."
HOMEPAGE = "https://github.com/yoriyuki/Camomile"
SECTION = "ocaml/devel"
LICENSE = "LGPL-2.1-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=d8045f3b8f929c1cb29a1e3fd737b499"

DEPENDS = " \
    camlp4-native \
"

SRC_URI = " \
    http://github.com/yoriyuki/Camomile/releases/download/rel-${PV}/camomile-${PV}.tar.bz2 \
    file://ocaml-camomile-destdir.patch \
    file://bytecode-ocamlbest.patch \
    file://unimap.patch \
"
SRC_URI[md5sum] = "1e25b6cd4efd26ab38a667db18d83f02"
SRC_URI[sha256sum] = "85806b051cf059b93676a10a3f66051f7f322cad6e3248172c3e5275f79d7100"

S = "${UNPACKDIR}/camomile-${PV}"

inherit ocaml findlib

FILES:${PN} += " \
    ${datadir}/camomile/charmaps/* \
    ${datadir}/camomile/database/* \
    ${datadir}/camomile/locales/* \
    ${datadir}/camomile/mappings/* \
"
do_configure() {
    ./configure \
        --prefix=${prefix} \
        --bindir=${bindir} \
        --libdir=${libdir} \
        --datadir=${datadir} \
        --host=${TARGET_SYS}
}

# Apparently the .cmi files are not available when they need to be.
#  Error: Unbound module <X>
# This does not happen using camlp4 byte-code instead of native compiled.
PARALLEL_MAKE = ""

# Database-generation tools must run on the build host. Cross ocamlc
# custom-links .byte executables with the target dynamic linker
# (/lib/ld-linux-x86-64.so.2), which is absent on Debian/Ubuntu hosts
# (see ocaml-cross / ocaml.bbclass). Host-native OCaml produces ocamlrun
# scripts; bytecode libraries are architecture-independent.
#
# After data/tools are ready, build target `opt` (.cmxa) with the cross
# ocamlopt so consumers such as dbd can link native programs.
do_compile() {
    export OCAMLLIB="${STAGING_LIBDIR_NATIVE}/ocaml"
    export OCAML_TOPLEVEL_PATH="${STAGING_LIBDIR_NATIVE}"
    # Drop stale cross-linked .byte tools from prior failed builds so make
    # rebuilds them as host-runnable ocamlrun scripts.
    rm -f tools/*.byte tools/*.opt
    oe_runmake byte unidata unimaps charmap_data locale_data \
        OCAMLC="${STAGING_BINDIR_NATIVE}/ocamlc" \
        OCAMLOPT="${STAGING_BINDIR_NATIVE}/ocamlopt"

    export OCAMLLIB="${STAGING_LIBDIR_NATIVE}/${TARGET_SYS}/ocaml"
    export OCAML_TOPLEVEL_PATH="${STAGING_LIBDIR_NATIVE}/${TARGET_SYS}"
    oe_runmake opt \
        OCAMLC="${OCAMLC}" \
        OCAMLOPT="${OCAMLOPT}"
}

do_install() {
    oe_runmake DESTDIR="${D}" install
}

INSANE_SKIP:${PN}-dev = "file-rdeps"
