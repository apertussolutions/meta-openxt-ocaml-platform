SUMMARY = "Camlp4 is a software system for writing extensible parsers for programming languages."
DESCRIPTION = "provides a set of OCaml libraries that are used to define \
grammars as well as loadable syntax extensions of such grammars. Camlp4 stands \
for Caml Preprocessor and Pretty-Printer and one of its most important \
applications is the definition of domain-specific extensions of the syntax of \
OCaml."
HOMEPAGE = "https://github.com/ocaml/camlp4"
SECTION = "ocaml/devel"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://LICENSE;md5=77f085d1023152a31ada8288ffd6e8f1"

# Versions are <maj>.<min>+<micro>, but the tarball uses <maj>.<min>-<micro>.
OPV = "${@"${PV}".replace('+', '-')}"

SRC_URI = " \
    https://github.com/ocaml/camlp4/archive/${PV}.tar.gz \
"
SRC_URI[md5sum] = "0042c09026ce4b3a06d932fee864939a"
SRC_URI[sha256sum] = "db242c0f4896ddd9dc7d6e8064cba8bf099fb9a4d4f5ed35120978501cd3a935"

DEPENDS = " \
    ocamlbuild-native \
    coreutils-native \
"

S = "${UNPACKDIR}/${BPN}-${OPV}"

inherit native ocaml

do_configure() {
    ./configure \
        --bindir=${D}${bindir} \
        --libdir=${D}${libdir}/ocaml
}

do_compile() {
    # Contamination from cross toolchain to native toolchain is easier to
    # work-around using native camlp4o.opt.
    oe_runmake native
}

do_install() {
    oe_runmake install
    # TODO: Conditional to native being built without byte-code.
    ln -s camlp4of.opt ${D}${bindir}/camlp4of
    ln -s camlp4oof.opt ${D}${bindir}/camlp4oof
    ln -s camlp4o.opt ${D}${bindir}/camlp4o
    ln -s camlp4orf.opt ${D}${bindir}/camlp4orf
    ln -s camlp4rf.opt ${D}${bindir}/camlp4rf
    ln -s camlp4r.opt ${D}${bindir}/camlp4r
}
