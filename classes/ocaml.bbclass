# OCAMLLIB
# The directory containing the OCaml standard library. (If OCAMLLIB is not set,
# CAMLLIB will be used instead.) Used to locate the ld.conf configuration file
# for dynamic loading
OCAMLLIB:class-native = "${STAGING_LIBDIR_NATIVE}/ocaml"
OCAMLLIB:class-cross = "${STAGING_LIBDIR_NATIVE}/ocaml"
OCAMLLIB = "${STAGING_LIBDIR_NATIVE}/${TARGET_SYS}/ocaml"
export OCAMLLIB

# OCAML_TOPLEVEL_PATH
# Path to the top-level (path to libraries & such).
OCAML_TOPLEVEL_PATH:class-native = "${STAGING_LIBDIR_NATIVE}"
OCAML_TOPLEVEL_PATH:class-cross = "${STAGING_LIBDIR_NATIVE}"
OCAML_TOPLEVEL_PATH = "${STAGING_LIBDIR_NATIVE}/${TARGET_SYS}"
export OCAML_TOPLEVEL_PATH

# OCAML toolchain
# Common variables used in Makfiles for OCAML projects.
OCAMLMKLIB = "ocamlmklib -ldopt '--sysroot=${STAGING_DIR_NATIVE}'"
OCAMLC:class-target = "ocamlc -cc '${CC} -fPIC'"
OCAMLOPT:class-target = "ocamlopt -cc '${CC} -fPIC'"
OCAMLMKLIB:class-target = "ocamlmklib -ldopt '--sysroot=${STAGING_DIR_TARGET} ${LDFLAGS}'"

# Override Makefile variables with oe_runmake.
EXTRA_OEMAKE:append = ' \
    OCAMLMKLIB = "${OCAMLMKLIB}" \
'
# yacc/lex must run on the build host. Cross installs put unprefixed
# ocamlyacc/ocamllex earlier on PATH (usr/bin/${TARGET_SYS}/), and those
# binaries use the target dynamic linker (e.g. /lib/ld-linux-x86-64.so.2)
# which is missing on modern hosts — exec fails with ENOENT.
EXTRA_OEMAKE:append = ' \
    OCAMLYACC="${STAGING_BINDIR_NATIVE}/ocamlyacc" \
    OCAMLLEX="${STAGING_BINDIR_NATIVE}/ocamllex" \
'
EXTRA_OEMAKE:append:class-target = ' \
    OCAMLC="${OCAMLC}" \
    OCAMLOPT="${OCAMLOPT}" \
'

DEPENDS:append:class-native = " \
    ocaml-native \
"
DEPENDS:append:class-cross = " \
    ocaml-native \
"
DEPENDS:append:class-target = " \
    virtual/${TARGET_PREFIX}ocamlc \
    virtual/${TARGET_PREFIX}ocamlopt \
"

ocamllibdir = "${libdir}/ocaml"
export ocamllibdir

# cross environment ocaml headers to interface with C sources.
ocamlincdir = "${STAGING_LIBDIR_NATIVE}/${TARGET_SYS}/ocaml"
export ocamlincdir

OPN ?= "${@d.getVar('BPN').replace('ocaml-','')}"
