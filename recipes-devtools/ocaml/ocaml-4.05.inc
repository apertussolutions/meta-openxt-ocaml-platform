require ocaml-common.inc

LIC_FILES_CHKSUM = "file://LICENSE;md5=4f72f33f302a53dc329f4d3819fe14f9"

SRC_URI = " \
    http://caml.inria.fr/pub/distrib/ocaml-4.05/ocaml-${PV}.tar.xz \
    file://oe-linux-configure.patch \
    file://shebang-use-env.patch \
"
SRC_URI[md5sum] = "f13d9a58bcbdcb495016af4f709ebd16"
SRC_URI[sha256sum] = "04a527ba14b4d7d1b2ea7b2ae21aefecfa8d304399db94f35a96df1459e02ef9"

S = "${WORKDIR}/ocaml-${PV}"

CFLAGS += "-Wno-restrict"

do_configure() {
    ./configure -no-curses \
                -no-graph \
		-no-ocamldoc \
                -prefix ${prefix} \
                -libdir ${libdir}/ocaml \
                -mandir ${datadir}/man \
                -fPIC \
                ${EXTRA_CONF}
}
do_configure_append() {
    # Make sure to turn off GCC stack-protector.
    sed -i'' -re 's/NATIVECCCOMPOPTS=(.*)/NATIVECCCOMPOPTS=\1 -fno-stack-protector -fcommon/' config/Makefile
    sed -i'' -re 's/BYTECCCOMPOPTS=(.*)/BYTECCCOMPOPTS=\1 -fno-stack-protector -fcommon/' config/Makefile
}

do_compile() {
    # Build the native compiler and libraries as well (Xen needs ocamlopt...)
    oe_runmake world.opt
}

do_install() {
    oe_runmake DESTDIR="${D}" install
}
