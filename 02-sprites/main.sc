using import glm
using import format
using import Option
using import struct
using import String
using import Array

struct VertexAttributes
    position : vec2
    color : vec4

fn gen-vertices ()
    local vertices =
        arrayof vec2
            vec2 0.0   0.5
            vec2 -0.5 -0.5
            vec2 0.5  -0.5
    local colors =
        arrayof vec4
            vec4 1.0 0.0 0.0 1.0
            vec4 0.0 1.0 0.0 1.0
            vec4 0.0 0.0 1.0 1.0
    local vertex-data : (Array VertexAttributes)
    for i in (range 3)
        'append vertex-data
            VertexAttributes
                position = (vertices @ i)
                color = (colors @ i)
    vertex-data

import bottle
import bottle.src.gpu.common
using bottle.gpu.types
import wgpu

from (import bottle.src.gpu.binding-interface) let GPUResourceBinding
from (import bottle.src.gpu.bindgroup) let GPUBindGroup


vvv bind shader
..
    format
        """"struct VertexAttributes \{
                position: vec2<f32>,
                color: vec4<f32>,
            };

            @group(0)
            @binding(0)
            var<storage, read> vertexData: array<VertexAttributes>;

            struct VertexOutput \{
                @location(0) vcolor: vec4<f32>,
                @builtin(position) position: vec4<f32>,
            };

            @vertex
            fn vs_main(@builtin(vertex_index) vindex: u32) -> VertexOutput \{
                var out: VertexOutput;
                let attr = vertexData[vindex];
                out.position = vec4<f32>(attr.position, 0.0, 1.0);
                out.vcolor = attr.color;
                return out;
            }

            @fragment
            fn fs_main(vertex: VertexOutput) -> @location(0) vec4<f32> \{
                return vertex.vcolor;
            }


        attr_stride = (sizeof VertexAttributes)

@@ 'on bottle.configure
fn (cfg)
    cfg.window.title = "flying quads"

global pipeline : (Option GPUPipeline)
global bgroup0 : (Option GPUBindGroup)
global bgroup1 : (Option GPUBindGroup)
global vertices : (Array VertexAttributes)
global vertex-buffer : (Option GPUBuffer)

@@ 'on bottle.load
fn ()
    let dummies = bottle.src.gpu.common.istate.dummy-resources
    let cache = bottle.src.gpu.common.istate.cached-layouts
    # TODO: maybe we'd prefer the shader type to be an enum?
    local _pipeline = (GPUPipeline "Basic" (GPUShaderModule shader 'wgsl))
    let layout =
        try
            'get cache.bind-group-layouts S"StreamingMesh"
        else
            error "you made a typo you dofus"

    vertices = (gen-vertices)
    let bufsize =
        *
            sizeof ((typeof vertices) . ElementType)
            countof vertices
    let vbuffer = (GPUBuffer bufsize)
    'write (view vbuffer) vertices

    bgroup0 =
        GPUBindGroup layout
            GPUResourceBinding.Buffer
                buffer = vbuffer.handle
                offset = 0
                size = bufsize
            dummies.sampler
            dummies.texture-view

    vertex-buffer = vbuffer

    let layout =
        try
            'get cache.bind-group-layouts S"Uniforms"
        else
            error "you made a typo you dofus"

    bgroup1 =
        GPUBindGroup layout
            dummies.buffer

    pipeline = _pipeline


@@ 'on bottle.draw
fn (render-pass)
    let pipeline = ('force-unwrap pipeline)

    'set-pipeline render-pass pipeline
    'set-bindgroup render-pass 0:u32 (('force-unwrap bgroup0) . _handle)
    'set-bindgroup render-pass 1:u32 (('force-unwrap bgroup1) . _handle)

    'draw render-pass 3:u32 1:u32 0:u32 0:u32

bottle.run;
