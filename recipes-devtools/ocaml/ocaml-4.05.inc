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

# GCC 14 treats incompatible pointer types as errors; OCaml 4.05 needs relax.
CFLAGS += " \
    -Wno-restrict \
    -Wno-error=incompatible-pointer-types \
    -Wno-incompatible-pointer-types \
    -Wno-error=int-conversion \
    -Wno-int-conversion \
    -fcommon \
"

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
    # Turn off stack-protector; inject GCC 14 relax flags into baked opts.
    sed -i'' -re 's/NATIVECCCOMPOPTS=(.*)/NATIVECCCOMPOPTS=\1 -fno-stack-protector -fcommon -Wno-incompatible-pointer-types -Wno-int-conversion/' config/Makefile
    sed -i'' -re 's/BYTECCCOMPOPTS=(.*)/BYTECCCOMPOPTS=\1 -fno-stack-protector -fcommon -Wno-incompatible-pointer-types -Wno-int-conversion/' config/Makefile
    # SIGSTKSZ is not a compile-time constant on modern glibc
    for f in asmrun/signals_asm.c byterun/signals.c; do
        if [ -f ${S}/$f ]; then
            sed -i 's/static char sig_alt_stack\[SIGSTKSZ\]/static char sig_alt_stack[8192]/' ${S}/$f
        fi
    done
    # Host configure probes omit many POSIX features when runtest/hasgot
    # cannot execute (dynamic-linker path quirks on modern hosts). Force the
    # standard Linux Unix feature set so findlib and ocaml libs work.
    if [ -f ${S}/config/s.h ]; then
        for def in \
            HAS_SOCKETS HAS_INET_ATON HAS_IPV6 HAS_GETADDRINFO HAS_GETNAMEINFO \
            HAS_SOCKETPAIR HAS_UTIME HAS_UTIMES HAS_TRUNCATE HAS_FTRUNCATE \
            HAS_GETCWD HAS_GETWD HAS_GETPRIORITY HAS_SETPRIORITY \
            HAS_GETGROUPS HAS_SETGROUPS HAS_INITGROUPS \
            HAS_TERMIOS HAS_TERMIOS_H HAS_SYS_IOCTL_H \
            HAS_UNISTD HAS_DIRENT HAS_REWINDDIR HAS_LOCKF \
            HAS_MKFIFO HAS_GETHOSTBYNAME_R HAS_GETHOSTBYADDR_R \
            HAS_NANOSLEEP HAS_CLOCK_GETTIME HAS_MMAP HAS_MUNMAP \
            HAS_SELECT HAS_SETITIMER HAS_GETITIMER HAS_PUTENV \
            HAS_LOCALE_H HAS_SETLOCALE HAS_MKSTEMP HAS_SYS_WAIT_H \
            HAS_WAITPID HAS_WAIT4 HAS_GETRUSAGE HAS_TIMES \
            HAS_GETTIMEOFDAY HAS_MKTIME HAS_TM_ISDST HAS_TM_GMTOFF \
            HAS_SOCKET HAS_SOCKLEN_T HAS_UNIX_SOCKETS \
            HAS_SYMLINK HAS_READLINK HAS_LINK HAS_REALPATH \
            HAS_CHOWN HAS_FCHMOD HAS_FCHOWN HAS_LSTAT HAS_TRUNCATE64
        do
            grep -q "$def" ${S}/config/s.h || echo "#define $def" >> ${S}/config/s.h
        done
    fi
}

do_compile() {
    # Build the native compiler and libraries as well (Xen needs ocamlopt...)
    oe_runmake world.opt
}

do_install() {
    oe_runmake DESTDIR="${D}" install
}
