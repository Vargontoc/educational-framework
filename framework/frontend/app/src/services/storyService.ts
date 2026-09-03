import { useParentalAuthStore } from "@/stores/parentalAuth"
import apiClient from "./api"

export interface StoryPage {
    page: number
    text: string
}
export interface Story {
    title: string
    pages: StoryPage[]
}

export interface StoryEntry {
    id: string
    title: string
}


export async function getCatalog() : Promise<StoryEntry[]> {
    const token = useParentalAuthStore().token
    if(!token) return [];

    try {
        const response = await apiClient.get<StoryEntry[]>(`/api/v1/stories`, {},
            { Authorization: `Bearer ${token}` }
        )

        return response;
    }catch {
        return []
    }
}

export async function getStory(id: string) {
    const token = useParentalAuthStore().token
    if(!token) return null;

    try {
        const story = await apiClient.get<Story>(`/api/v1/stories/${id}`, {}, { Authorization: `Bearer ${token}`  })
        return story;
    }catch { return null }
}

export async function getCover(id: string) : Promise<Blob | null> {
    const token = useParentalAuthStore().token
    if(!token) return null;

    try {
        return await apiClient.getBlob(`/api/v1/stories/${id}/cover`,
            { Authorization: `Bearer ${token}` }
        )
    } catch {
        return null
    }
}

export async function getImage(id: string, page: number) : Promise<Blob | null> {
    const token = useParentalAuthStore().token
    if(!token) return null;

    try {
        return await apiClient.getBlob(`/api/v1/stories/${id}/pages/${page}/image`,
            { Authorization: `Bearer ${token}` }
        )
    } catch {
        return null
    }
}

export async function getAudio(id: string, page: number) : Promise<Blob | null> {
    const token = useParentalAuthStore().token
    if(!token) return null;

    try {
        return await apiClient.getBlob(`/api/v1/stories/${id}/pages/${page}/audio`,
            { Authorization: `Bearer ${token}` }
        )
    } catch {
        return null
    }
}