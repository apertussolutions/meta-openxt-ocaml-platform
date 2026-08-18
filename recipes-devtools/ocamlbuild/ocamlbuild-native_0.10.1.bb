SUMMARY = "OCamlbuild generic build tool."
DESCRIPTION = "OCamlbuild is a generic build tool, that has built-in rules for \
building OCaml library and programs."
HOMEPAGE = "https://github.com/ocaml/ocamlbuild"
SECTION = "devel/ocaml"
LICENSE = "LGPL-2.1-only"

LIC_FILES_CHKSUM = "file://LICENSE;md5=5123b1988300c0d24c79e04f09d86dc0"

SRC_URI = " \
    git://github.com/ocaml/ocamlbuild.git;protocol=https;tag=${PV};nobranch=1 \
    file://shebang-length-overflow.patch \
"
SRCREV = "dcde2bde5f2b75b8ac4599ddce74052298420bbc"

S = "${UNPACKDIR}/${BP}"

inherit native ocaml

# Bytecode ocamlbuild.byte is a custom runtime + appended bytecode. OE's
# package/sysroot strip removes the bytecode trailer and leaves a bare
# ocamlrun that rejects every ocamlbuild flag (e.g. -classic-display).
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"

do_configure() {
    oe_runmake configure \
        OCAMLBUILD_PREFIX=${D}${prefix} \
        OCAMLBUILD_BINDIR=${D}${bindir} \
        OCAMLBUILD_LIBDIR=${D}${libdir}/ocaml \
        OCAMLBUILD_MANDIR=${D}${datadir}/man \
        OCAML_NATIVE=false \
        OCAML_NATIVE_TOOLS=false
}

do_compile() {
    oe_runmake
}

do_install() {
    oe_runmake install CHECK_IF_PREINSTALLED=false
}
